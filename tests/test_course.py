"""
test_course.py - 课程模块测试
=============================
此文件包含课程相关的所有测试用例，包括：
1. 课程获取测试（列表、详情、搜索、筛选、章节）
2. 课程创建测试（教师创建、权限控制、参数校验）
3. 课程集成测试

测试分类标记：
- @pytest.mark.course: 课程接口测试
- @pytest.mark.smoke: 冒烟测试（核心功能）
- @pytest.mark.integration: 集成测试（完整流程）
"""

import pytest
from utils.api_client import ApiClient, assert_success, assert_error


@pytest.mark.course
@pytest.mark.smoke
class TestCourseSmoke:
    """
    课程冒烟测试
    
    验证课程最核心的功能是否正常工作。
    """

    def test_get_course_list(self, base_url):
        """
        测试获取课程列表
        
        验证未登录用户可以获取课程列表（公开接口）。
        """
        client = ApiClient(base_url)
        status, data = client.get("/courses")
        assert_success((status, data), "获取课程列表失败")
        assert isinstance(data["data"], list), "课程列表应为数组"
        client.close()


@pytest.mark.course
class TestCourseRetrieval:
    """
    课程获取测试类
    
    测试课程的各种获取方式：列表、详情、搜索、筛选、章节。
    """

    def test_get_course_detail(self, base_url):
        """
        测试获取课程详情
        
        验证可以获取单个课程的详细信息，包括章节列表。
        """
        client = ApiClient(base_url)
        status, data = client.get("/courses")
        assert_success((status, data), "获取课程列表失败")
        
        if data["data"]:
            course_id = data["data"][0]["id"]
            status, data = client.get(f"/courses/{course_id}")
            assert_success((status, data), "获取课程详情失败")
            assert data["data"]["id"] == course_id, "返回的课程ID不匹配"
            assert "title" in data["data"], "响应缺少 title"
            assert "subject" in data["data"], "响应缺少 subject"
            assert "chapters" in data["data"], "响应缺少 chapters"
        client.close()

    @pytest.mark.parametrize("keyword", ["语文", "数学", "英语"])
    def test_search_courses(self, base_url, keyword):
        """
        参数化测试：搜索课程
        
        验证可以按关键词搜索课程。
        
        Args:
            keyword: 搜索关键词
        """
        client = ApiClient(base_url)
        status, data = client.get(f"/courses?keyword={keyword}")
        assert_success((status, data), "搜索课程失败")
        assert isinstance(data["data"], list), "搜索结果应为数组"
        client.close()

    @pytest.mark.parametrize("subject,grade", [
        ("数学", "初一"),
        ("语文", "初二"),
        ("英语", "初三"),
    ])
    def test_filter_courses(self, base_url, subject, grade):
        """
        参数化测试：筛选课程
        
        验证可以按科目和年级筛选课程。
        
        Args:
            subject: 科目
            grade: 年级
        """
        client = ApiClient(base_url)
        status, data = client.get(f"/courses?subject={subject}&grade={grade}")
        assert_success((status, data), "筛选课程失败")
        assert isinstance(data["data"], list), "筛选结果应为数组"
        client.close()

    def test_get_course_chapters(self, base_url):
        """
        测试获取课程章节
        
        验证可以获取课程的章节列表。
        """
        client = ApiClient(base_url)
        status, data = client.get("/courses")
        assert_success((status, data), "获取课程列表失败")
        
        if data["data"]:
            course_id = data["data"][0]["id"]
            status, data = client.get(f"/courses/{course_id}/chapters")
            assert_success((status, data), "获取课程章节失败")
            assert isinstance(data["data"], list), "章节列表应为数组"
        client.close()


@pytest.mark.course
class TestCourseCreation:
    """
    课程创建测试类
    
    测试课程的创建功能：教师创建、权限控制、参数校验。
    """

    def test_create_course_as_teacher(self, base_url, teacher_token):
        """
        测试教师创建课程
        
        验证教师可以正常创建课程，创建后状态为 PENDING。
        """
        client = ApiClient(base_url, teacher_token)
        status, data = client.post("/teacher/courses", {
            "title": "数学基础课程",
            "subject": "数学",
            "grade": "初二",
            "price": 199.0,
            "totalHours": 30,
            "description": "初二数学基础课程",
            "chapters": [
                {"title": "第一章：代数基础", "duration": 45},
                {"title": "第二章：几何入门", "duration": 50}
            ]
        })
        assert_success((status, data), "教师创建课程失败")
        assert data["data"]["title"] == "数学基础课程", "课程标题不匹配"
        assert data["data"]["status"] == "PENDING", "课程状态应为 PENDING"
        
        # 清理测试数据
        client.delete(f"/teacher/courses/{data['data']['id']}")
        client.close()

    def test_create_course_as_parent(self, base_url, parent_token):
        """
        测试家长创建课程
        
        验证家长角色不能创建课程，应返回权限错误。
        """
        client = ApiClient(base_url, parent_token)
        status, data = client.post("/teacher/courses", {
            "title": "非法课程",
            "subject": "语文",
            "grade": "初一",
            "price": 99.0,
            "totalHours": 10,
            "description": "非法创建",
            "chapters": []
        })
        assert_error((status, data), "家长不应创建课程")
        client.close()

    @pytest.mark.parametrize("field,value", [
        ("title", ""),
        ("subject", ""),
        ("grade", ""),
        ("price", ""),
        ("totalHours", ""),
        ("price", -1),
        ("totalHours", -1),
    ])
    def test_create_course_invalid_fields(self, base_url, teacher_token, field, value):
        """
        参数化测试：创建课程时的参数校验
        
        验证后端对课程创建参数的校验：
        - 必填字段为空
        - 价格为负数
        - 课时为负数
        
        Args:
            field: 要测试的字段名
            value: 无效值
        """
        client = ApiClient(base_url, teacher_token)
        data = {
            "title": "测试课程",
            "subject": "数学",
            "grade": "初一",
            "price": 99.0,
            "totalHours": 10,
            "description": "测试",
            "chapters": []
        }
        data[field] = value
        
        status, response_data = client.post("/teacher/courses", data)
        assert_error((status, response_data), f"无效{field}应返回失败")
        client.close()


@pytest.mark.course
@pytest.mark.integration
class TestCourseIntegration:
    """
    课程集成测试
    
    测试课程的完整生命周期：创建 → 审核 → 查看 → 更新 → 删除。
    """

    def test_full_course_workflow(self, base_url, teacher_token, admin_token):
        """
        测试完整的课程工作流程
        
        验证课程从创建到删除的完整生命周期。
        """
        client = ApiClient(base_url, teacher_token)
        
        # 1. 教师创建课程
        status, data = client.post("/teacher/courses", {
            "title": "集成测试课程",
            "subject": "物理",
            "grade": "高一",
            "price": 399.0,
            "totalHours": 50,
            "description": "集成测试",
            "chapters": [
                {"title": "第一章", "duration": 45},
                {"title": "第二章", "duration": 50}
            ]
        })
        assert_success((status, data), "创建课程失败")
        course_id = data["data"]["id"]
        
        # 2. 管理员审核课程
        client.set_token(admin_token)
        client.put(f"/admin/courses/{course_id}/approve")
        
        # 3. 公开查看课程详情
        client.clear_token()
        status, data = client.get(f"/courses/{course_id}")
        assert_success((status, data), "获取课程详情失败")
        assert data["data"]["status"] == "PUBLISHED", "课程状态应为 PUBLISHED"
        
        # 4. 教师更新课程
        client.set_token(teacher_token)
        status, data = client.put(f"/teacher/courses/{course_id}", {
            "title": "更新后的课程",
            "description": "更新后的描述"
        })
        assert_success((status, data), "更新课程失败")
        
        # 5. 教师删除课程（清理测试数据）
        client.delete(f"/teacher/courses/{course_id}")
        client.close()
