"""
test_teacher.py - 教师模块测试
==============================
此文件包含教师相关的所有测试用例，包括：
1. 教师课程管理（获取、更新、删除）
2. 教师学生管理
3. 教师通知管理
4. 教师权限控制测试
5. 教师集成测试

测试分类标记：
- @pytest.mark.teacher: 教师接口测试
- @pytest.mark.smoke: 冒烟测试（核心功能）
- @pytest.mark.integration: 集成测试（完整流程）
"""

import pytest
from utils.api_client import ApiClient, assert_success, assert_error


@pytest.mark.teacher
@pytest.mark.smoke
class TestTeacherSmoke:
    """
    教师冒烟测试
    
    验证教师最核心的功能是否正常工作。
    """

    def test_teacher_get_my_courses(self, base_url, teacher_token):
        """
        测试教师获取自己的课程列表
        
        验证教师可以获取自己创建的课程列表。
        """
        client = ApiClient(base_url, teacher_token)
        status, data = client.get("/teacher/courses")
        assert_success((status, data), "教师获取自己课程失败")
        assert isinstance(data["data"], list), "课程列表应为数组"
        client.close()


@pytest.mark.teacher
class TestTeacherCourses:
    """
    教师课程管理测试类
    
    测试教师对课程的管理功能：获取、更新、删除。
    """

    def test_teacher_update_course(self, base_url, teacher_token):
        """
        测试教师更新课程
        
        验证教师可以更新自己创建的课程信息。
        """
        client = ApiClient(base_url, teacher_token)
        status, data = client.get("/teacher/courses")
        assert_success((status, data), "获取教师课程失败")
        
        if data["data"]:
            course_id = data["data"][0]["id"]
            status, data = client.put(f"/teacher/courses/{course_id}", {
                "title": "更新后的课程名称",
                "description": "更新后的描述"
            })
            assert_success((status, data), "教师更新课程失败")
            assert data["data"]["title"] == "更新后的课程名称", "课程标题更新失败"
        client.close()

    def test_teacher_delete_course(self, base_url, teacher_token):
        """
        测试教师删除课程
        
        验证教师可以删除自己创建的课程。
        """
        client = ApiClient(base_url, teacher_token)
        
        # 创建待删除的课程
        status, data = client.post("/teacher/courses", {
            "title": "待删除课程",
            "subject": "语文",
            "grade": "初一",
            "price": 99.0,
            "totalHours": 10,
            "description": "测试删除",
            "chapters": []
        })
        assert_success((status, data), "创建待删除课程失败")
        
        # 删除课程
        course_id = data["data"]["id"]
        status, data = client.delete(f"/teacher/courses/{course_id}")
        assert_success((status, data), "教师删除课程失败")
        
        client.close()


@pytest.mark.teacher
class TestTeacherStudents:
    """
    教师学生管理测试类
    
    测试教师对学生的管理功能。
    """

    def test_teacher_get_students(self, base_url, teacher_token):
        """
        测试教师获取学生列表
        
        验证教师可以获取自己课程下的学生列表。
        """
        client = ApiClient(base_url, teacher_token)
        status, data = client.get("/teacher/students")
        assert_success((status, data), "教师获取学生列表失败")
        assert isinstance(data["data"], list), "学生列表应为数组"
        client.close()


@pytest.mark.teacher
class TestTeacherNotifications:
    """
    教师通知管理测试类
    
    测试教师发送和获取通知的功能。
    """

    def test_teacher_send_notification(self, base_url, teacher_token):
        """
        测试教师发送通知
        
        验证教师可以向学生发送通知。
        """
        client = ApiClient(base_url, teacher_token)
        status, data = client.post("/teacher/notifications", {
            "title": "测试通知",
            "content": "这是一条测试通知",
            "studentIds": []
        })
        assert_success((status, data), "教师发送通知失败")
        client.close()

    def test_teacher_get_notifications(self, base_url, teacher_token):
        """
        测试教师获取通知列表
        
        验证教师可以获取自己发送的通知列表。
        """
        client = ApiClient(base_url, teacher_token)
        status, data = client.get("/teacher/notifications")
        assert_success((status, data), "教师获取通知列表失败")
        assert isinstance(data["data"], list), "通知列表应为数组"
        client.close()


@pytest.mark.teacher
class TestTeacherAuthorization:
    """
    教师权限控制测试类
    
    验证教师接口的访问权限控制：
    - 非教师不能访问教师接口
    - 无 token 不能访问教师接口
    """

    def test_parent_access_teacher_api(self, base_url, parent_token):
        """
        测试家长访问教师接口
        
        验证家长角色不能访问教师接口，应返回权限错误。
        """
        client = ApiClient(base_url, parent_token)
        status, data = client.get("/teacher/courses")
        assert_error((status, data), "家长不应访问教师接口")
        client.close()

    def test_no_token_access_teacher_api(self, base_url):
        """
        测试无 token 访问教师接口
        
        验证未登录用户不能访问教师接口。
        """
        client = ApiClient(base_url)
        status, data = client.get("/teacher/courses")
        assert_error((status, data), "无token不应访问教师接口")
        client.close()


@pytest.mark.teacher
@pytest.mark.integration
class TestTeacherIntegration:
    """
    教师集成测试
    
    测试教师的完整工作流程。
    """

    def test_full_teacher_workflow(self, base_url, teacher_token):
        """
        测试完整的教师工作流程
        
        验证教师从创建课程到删除课程的完整流程。
        """
        client = ApiClient(base_url, teacher_token)
        
        # 1. 创建课程
        status, data = client.post("/teacher/courses", {
            "title": "教师集成测试课程",
            "subject": "化学",
            "grade": "初三",
            "price": 259.0,
            "totalHours": 35,
            "description": "集成测试",
            "chapters": [{"title": "第一章", "duration": 45}]
        })
        course_id = data["data"]["id"]
        
        # 2. 获取课程列表
        status, data = client.get("/teacher/courses")
        assert_success((status, data), "获取课程列表失败")
        
        # 3. 更新课程
        status, data = client.put(f"/teacher/courses/{course_id}", {
            "title": "更新课程"
        })
        assert_success((status, data), "更新课程失败")
        
        # 4. 删除课程（清理测试数据）
        client.delete(f"/teacher/courses/{course_id}")
        client.close()
