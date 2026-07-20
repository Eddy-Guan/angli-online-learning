"""
test_evaluation.py - 评价模块测试
==================================
此文件包含评价相关的所有测试用例，包括：
1. 获取课程评价列表
2. 创建评价
3. 获取我的评价列表
4. 更新评价
5. 删除评价
6. 参数校验测试（评分范围、空内容）
7. 评价集成测试

测试分类标记：
- @pytest.mark.evaluation: 评价接口测试
- @pytest.mark.smoke: 冒烟测试（核心功能）
- @pytest.mark.integration: 集成测试（完整流程）
"""

import pytest
from utils.api_client import ApiClient, assert_success, assert_error


@pytest.mark.evaluation
@pytest.mark.smoke
class TestEvaluationSmoke:
    """
    评价冒烟测试
    
    验证评价最核心的功能是否正常工作。
    """

    def test_get_evaluations_by_course(self, base_url):
        """
        测试获取课程评价列表
        
        验证可以获取指定课程的评价列表（公开接口）。
        如果系统中有已发布的课程，则取第一个课程测试。
        """
        client = ApiClient(base_url)
        status, data = client.get("/courses?status=PUBLISHED")
        assert_success((status, data), "获取已发布课程失败")
        
        if data["data"]:
            course_id = data["data"][0]["id"]
            status, data = client.get(f"/evaluations/course/{course_id}")
            assert_success((status, data), "获取课程评价失败")
            assert isinstance(data["data"], list), "评价列表应为数组"
        client.close()


@pytest.mark.evaluation
class TestEvaluationManagement:
    """
    评价管理测试类
    
    测试评价的各种操作：创建、获取、更新、删除。
    """

    def test_create_evaluation(self, base_url, parent_token, teacher_token, admin_token):
        """
        测试创建评价
        
        完整流程：教师创建课程 → 管理员审核 → 家长创建评价。
        """
        client = ApiClient(base_url, teacher_token)
        
        # 1. 教师创建课程
        status, data = client.post("/teacher/courses", {
            "title": "评价测试课程",
            "subject": "数学",
            "grade": "初一",
            "price": 99.0,
            "totalHours": 20,
            "description": "测试",
            "chapters": [{"title": "第一章", "duration": 45}]
        })
        course_id = data["data"]["id"]
        
        # 2. 管理员审核课程
        client.set_token(admin_token)
        client.put(f"/admin/courses/{course_id}/approve")
        
        # 3. 家长创建评价
        client.set_token(parent_token)
        status, data = client.post("/evaluations", {
            "courseId": course_id,
            "rating": 5,
            "content": "课程非常好！"
        })
        assert_success((status, data), "创建评价失败")
        assert data["data"]["rating"] == 5, "评分数值不匹配"
        
        # 清理测试数据
        client.set_token(teacher_token)
        client.delete(f"/teacher/courses/{course_id}")
        client.close()

    def test_get_my_evaluations(self, base_url, parent_token):
        """
        测试获取我的评价列表
        
        验证家长可以获取自己发布的评价列表。
        """
        client = ApiClient(base_url, parent_token)
        status, data = client.get("/evaluations/my")
        assert_success((status, data), "获取我的评价失败")
        assert isinstance(data["data"], list), "评价列表应为数组"
        client.close()

    def test_update_evaluation(self, base_url, parent_token):
        """
        测试更新评价
        
        验证家长可以更新自己发布的评价。
        """
        client = ApiClient(base_url, parent_token)
        status, data = client.get("/evaluations/my")
        assert_success((status, data), "获取我的评价失败")
        
        if data["data"]:
            eval_id = data["data"][0]["id"]
            status, data = client.put(f"/evaluations/{eval_id}", {
                "rating": 4,
                "content": "更新后的评价"
            })
            assert_success((status, data), "更新评价失败")
            assert data["data"]["rating"] == 4, "评分更新失败"
        client.close()

    def test_delete_evaluation(self, base_url, parent_token, teacher_token, admin_token):
        """
        测试删除评价
        
        完整流程：教师创建课程 → 管理员审核 → 家长创建评价 → 家长删除评价。
        """
        client = ApiClient(base_url, teacher_token)
        
        # 1. 教师创建课程
        status, data = client.post("/teacher/courses", {
            "title": "删除评价测试",
            "subject": "语文",
            "grade": "初二",
            "price": 199.0,
            "totalHours": 30,
            "description": "测试",
            "chapters": [{"title": "第一章", "duration": 45}]
        })
        course_id = data["data"]["id"]
        
        # 2. 管理员审核课程
        client.set_token(admin_token)
        client.put(f"/admin/courses/{course_id}/approve")
        
        # 3. 家长创建评价
        client.set_token(parent_token)
        status, data = client.post("/evaluations", {
            "courseId": course_id,
            "rating": 3,
            "content": "待删除评价"
        })
        eval_id = data["data"]["id"]
        
        # 4. 家长删除评价
        status, data = client.delete(f"/evaluations/{eval_id}")
        assert_success((status, data), "删除评价失败")
        
        # 清理测试数据
        client.set_token(teacher_token)
        client.delete(f"/teacher/courses/{course_id}")
        client.close()


@pytest.mark.evaluation
class TestEvaluationValidation:
    """
    评价参数校验测试类
    
    测试评价相关的参数校验：评分范围、内容为空。
    """

    @pytest.mark.parametrize("rating", [0, 6, -1, 7])
    def test_create_evaluation_invalid_rating(self, base_url, parent_token, rating):
        """
        参数化测试：无效评分
        
        验证评分必须在 1-5 之间，超出范围应返回失败。
        
        Args:
            rating: 无效评分值
        """
        client = ApiClient(base_url)
        status, data = client.get("/courses?status=PUBLISHED")
        assert_success((status, data), "获取已发布课程失败")
        
        if data["data"]:
            course_id = data["data"][0]["id"]
            client.set_token(parent_token)
            status, data = client.post("/evaluations", {
                "courseId": course_id,
                "rating": rating,
                "content": "无效评分"
            })
            assert_error((status, data), f"无效评分{rating}应返回失败")
        client.close()

    def test_create_evaluation_empty_content(self, base_url, parent_token):
        """
        测试评价内容为空
        
        验证评价内容不能为空，应返回失败。
        """
        client = ApiClient(base_url)
        status, data = client.get("/courses?status=PUBLISHED")
        assert_success((status, data), "获取已发布课程失败")
        
        if data["data"]:
            course_id = data["data"][0]["id"]
            client.set_token(parent_token)
            status, data = client.post("/evaluations", {
                "courseId": course_id,
                "rating": 5,
                "content": ""
            })
            assert_error((status, data), "空内容应返回失败")
        client.close()


@pytest.mark.evaluation
@pytest.mark.integration
class TestEvaluationIntegration:
    """
    评价集成测试
    
    测试评价的完整生命周期：创建 → 更新 → 删除。
    """

    def test_full_evaluation_workflow(self, base_url, parent_token, teacher_token, admin_token):
        """
        测试完整的评价工作流程
        
        验证评价从创建到删除的完整生命周期。
        """
        client = ApiClient(base_url, teacher_token)
        
        # 1. 教师创建课程
        status, data = client.post("/teacher/courses", {
            "title": "评价集成测试",
            "subject": "英语",
            "grade": "初三",
            "price": 299.0,
            "totalHours": 40,
            "description": "集成测试",
            "chapters": [{"title": "第一章", "duration": 45}]
        })
        course_id = data["data"]["id"]
        
        # 2. 管理员审核课程
        client.set_token(admin_token)
        client.put(f"/admin/courses/{course_id}/approve")
        
        # 3. 家长创建评价
        client.set_token(parent_token)
        status, data = client.post("/evaluations", {
            "courseId": course_id,
            "rating": 5,
            "content": "非常好的课程"
        })
        eval_id = data["data"]["id"]
        
        # 4. 更新评价
        client.put(f"/evaluations/{eval_id}", {
            "rating": 4,
            "content": "更新评价"
        })
        
        # 5. 删除评价
        client.delete(f"/evaluations/{eval_id}")
        
        # 6. 清理测试数据
        client.set_token(teacher_token)
        client.delete(f"/teacher/courses/{course_id}")
        client.close()
