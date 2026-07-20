"""
test_student.py - 学生模块测试
==============================
此文件包含学生（孩子）相关的所有测试用例，包括：
1. 家长获取自己的孩子列表
2. 家长绑定孩子（创建）
3. 家长更新孩子信息
4. 家长删除孩子
5. 参数校验测试
6. 学生集成测试

测试分类标记：
- @pytest.mark.student: 学生接口测试
- @pytest.mark.smoke: 冒烟测试（核心功能）
- @pytest.mark.integration: 集成测试（完整流程）
"""

import pytest
from utils.api_client import ApiClient, assert_success, assert_error


@pytest.mark.student
@pytest.mark.smoke
class TestStudentSmoke:
    """
    学生冒烟测试
    
    验证学生管理最核心的功能是否正常工作。
    """

    def test_parent_get_my_children(self, base_url, parent_token):
        """
        测试家长获取自己的孩子列表
        
        验证家长可以获取自己绑定的孩子列表。
        """
        client = ApiClient(base_url, parent_token)
        status, data = client.get("/students/my-children")
        assert_success((status, data), "家长获取自己孩子失败")
        assert isinstance(data["data"], list), "孩子列表应为数组"
        client.close()


@pytest.mark.student
class TestStudentManagement:
    """
    学生管理测试类
    
    测试家长对孩子的管理功能：绑定、更新、删除。
    """

    def test_bind_child_as_parent(self, base_url, parent_token):
        """
        测试家长绑定孩子
        
        验证家长可以绑定新的孩子信息。
        """
        client = ApiClient(base_url, parent_token)
        status, data = client.post("/students/bind", {
            "name": "测试孩子",
            "grade": "初一",
            "age": 13,
            "gender": "MALE"
        })
        assert_success((status, data), "家长绑定孩子失败")
        assert data["data"]["name"] == "测试孩子", "孩子姓名不匹配"
        
        # 清理测试数据
        client.delete(f"/students/{data['data']['id']}")
        client.close()

    @pytest.mark.parametrize("gender", ["MALE", "FEMALE"])
    def test_bind_child_various_gender(self, base_url, parent_token, gender):
        """
        参数化测试：绑定不同性别的孩子
        
        验证家长可以绑定不同性别的孩子。
        
        Args:
            gender: 性别（MALE 或 FEMALE）
        """
        client = ApiClient(base_url, parent_token)
        status, data = client.post("/students/bind", {
            "name": f"测试{gender}",
            "grade": "初二",
            "age": 14,
            "gender": gender
        })
        assert_success((status, data), f"绑定{gender}孩子失败")
        assert data["data"]["gender"] == gender, "孩子性别不匹配"
        
        # 清理测试数据
        client.delete(f"/students/{data['data']['id']}")
        client.close()

    def test_update_student(self, base_url, parent_token):
        """
        测试更新孩子信息
        
        验证家长可以更新已绑定孩子的信息。
        """
        client = ApiClient(base_url, parent_token)
        status, data = client.get("/students/my-children")
        assert_success((status, data), "获取孩子列表失败")
        
        if data["data"]:
            child_id = data["data"][0]["id"]
            status, data = client.put(f"/students/{child_id}", {
                "name": "更新后的名字",
                "grade": "初三"
            })
            assert_success((status, data), "更新孩子信息失败")
            assert data["data"]["name"] == "更新后的名字", "孩子姓名更新失败"
        client.close()

    def test_delete_student(self, base_url, parent_token):
        """
        测试删除孩子
        
        验证家长可以删除已绑定的孩子。
        """
        client = ApiClient(base_url, parent_token)
        
        # 创建待删除的孩子
        status, data = client.post("/students/bind", {
            "name": "待删除孩子",
            "grade": "一年级",
            "age": 7,
            "gender": "MALE"
        })
        assert_success((status, data), "创建待删除孩子失败")
        
        # 删除孩子
        child_id = data["data"]["id"]
        status, data = client.delete(f"/students/{child_id}")
        assert_success((status, data), "删除孩子失败")
        
        client.close()


@pytest.mark.student
class TestStudentValidation:
    """
    学生参数校验测试类
    
    测试绑定孩子时的参数校验。
    """

    @pytest.mark.parametrize("field,value", [
        ("name", ""),
        ("grade", ""),
        ("age", ""),
        ("gender", ""),
        ("age", -1),
        ("age", 0),
        ("age", 100),
    ])
    def test_bind_child_invalid_fields(self, base_url, parent_token, field, value):
        """
        参数化测试：绑定孩子时的无效字段
        
        验证后端对绑定孩子参数的校验：
        - 必填字段为空
        - 年龄为负数或超出合理范围
        
        Args:
            field: 要测试的字段名
            value: 无效值
        """
        client = ApiClient(base_url, parent_token)
        data = {
            "name": "测试孩子",
            "grade": "初一",
            "age": 13,
            "gender": "MALE"
        }
        data[field] = value
        
        status, response_data = client.post("/students/bind", data)
        assert_error((status, response_data), f"无效{field}应返回失败")
        client.close()


@pytest.mark.student
@pytest.mark.integration
class TestStudentIntegration:
    """
    学生集成测试
    
    测试家长管理孩子的完整流程。
    """

    def test_full_student_workflow(self, base_url, parent_token):
        """
        测试完整的孩子管理流程
        
        验证家长从绑定孩子到删除孩子的完整流程。
        """
        client = ApiClient(base_url, parent_token)
        
        # 1. 绑定孩子
        status, data = client.post("/students/bind", {
            "name": "集成测试孩子",
            "grade": "五年级",
            "age": 11,
            "gender": "FEMALE"
        })
        child_id = data["data"]["id"]
        
        # 2. 获取孩子列表
        status, data = client.get("/students/my-children")
        assert_success((status, data), "获取孩子列表失败")
        
        # 3. 更新孩子信息
        status, data = client.put(f"/students/{child_id}", {
            "name": "更新名字",
            "grade": "六年级"
        })
        assert_success((status, data), "更新孩子信息失败")
        
        # 4. 删除孩子（清理测试数据）
        client.delete(f"/students/{child_id}")
        client.close()
