"""
test_admin.py - 管理员模块测试
===============================
此文件包含管理员相关的所有测试用例，包括：
1. 仪表盘统计测试
2. 用户管理测试（获取列表、详情、审核教师）
3. 课程管理测试（获取列表、审核课程、下架课程）
4. 权限控制测试
5. 管理员集成测试

测试分类标记：
- @pytest.mark.admin: 管理员接口测试
- @pytest.mark.smoke: 冒烟测试（核心功能）
- @pytest.mark.integration: 集成测试（完整流程）
"""

import pytest
from utils.api_client import ApiClient, assert_success, assert_error


@pytest.mark.admin
@pytest.mark.smoke
class TestAdminSmoke:
    """
    管理员冒烟测试
    
    验证管理员最核心的功能是否正常工作。
    """

    def test_admin_dashboard_stats(self, base_url, admin_token):
        """
        测试获取管理员仪表盘统计
        
        验证管理员可以获取系统统计数据：
        - 总用户数
        - 总课程数
        - 待审核教师数
        - 待审核课程数
        """
        client = ApiClient(base_url, admin_token)
        status, data = client.get("/admin/stats")
        assert_success((status, data), "获取仪表盘统计失败")
        assert "totalUsers" in data["data"], "响应缺少 totalUsers"
        assert "totalCourses" in data["data"], "响应缺少 totalCourses"
        assert "pendingTeachers" in data["data"], "响应缺少 pendingTeachers"
        assert "pendingCourses" in data["data"], "响应缺少 pendingCourses"
        client.close()


@pytest.mark.admin
class TestAdminUsers:
    """
    管理员用户管理测试类
    
    测试管理员对用户的管理功能：获取列表、详情、审核教师。
    """

    def test_admin_get_users(self, base_url, admin_token):
        """
        测试获取用户列表
        
        验证管理员可以获取所有用户的列表。
        """
        client = ApiClient(base_url, admin_token)
        status, data = client.get("/admin/users")
        assert_success((status, data), "获取用户列表失败")
        assert isinstance(data["data"], list), "用户列表应为数组"
        client.close()

    def test_admin_get_user_detail(self, base_url, admin_token):
        """
        测试获取用户详情
        
        验证管理员可以获取单个用户的详细信息。
        如果系统中有用户，则取第一个用户测试。
        """
        client = ApiClient(base_url, admin_token)
        status, data = client.get("/admin/users")
        assert_success((status, data), "获取用户列表失败")
        
        if data["data"]:
            user_id = data["data"][0]["id"]
            status, data = client.get(f"/admin/users/{user_id}")
            assert_success((status, data), "获取用户详情失败")
            assert data["data"]["id"] == user_id, "返回的用户ID不匹配"
        client.close()

    @pytest.mark.parametrize("action", ["approve", "reject"])
    def test_admin_teacher_actions(self, base_url, admin_token, action):
        """
        参数化测试：审核/拒绝教师
        
        验证管理员可以审核或拒绝待审核的教师账号。
        
        Args:
            action: 操作类型（approve 或 reject）
        """
        import random
        phone = f"1{random.randint(5,9)}{random.randint(0,9)}{random.randint(0,9)}{random.randint(0,9)}{random.randint(0,9)}{random.randint(0,9)}{random.randint(0,9)}{random.randint(0,9)}{random.randint(0,9)}{random.randint(0,9)}"
        
        client = ApiClient(base_url)
        # 注册教师（状态为 PENDING）
        client.post("/auth/send-code", {"phone": phone})
        client.post("/auth/register", {
            "phone": phone, "password": "123456",
            "realName": "待审核教师", "role": "TEACHER", "code": "123456"
        })
        
        # 使用管理员账号进行审核或拒绝操作
        client.set_token(admin_token)
        status, data = client.put(f"/admin/users/{phone}/{action}")
        assert_success((status, data), f"{action}教师失败")
        
        client.close()

    def test_admin_approve_teacher_login(self, base_url, admin_token):
        """
        测试审核教师后可以登录
        
        完整流程：注册教师 → 审核 → 登录验证。
        """
        import random
        phone = f"1{random.randint(5,9)}{random.randint(0,9)}{random.randint(0,9)}{random.randint(0,9)}{random.randint(0,9)}{random.randint(0,9)}{random.randint(0,9)}{random.randint(0,9)}{random.randint(0,9)}{random.randint(0,9)}"
        
        client = ApiClient(base_url)
        # 注册教师
        client.post("/auth/send-code", {"phone": phone})
        client.post("/auth/register", {
            "phone": phone, "password": "123456",
            "realName": "待审核教师", "role": "TEACHER", "code": "123456"
        })
        
        # 管理员审核
        client.set_token(admin_token)
        client.put(f"/admin/users/{phone}/approve")
        
        # 教师登录验证
        client.clear_token()
        status, data = client.post("/auth/login", {
            "phone": phone, "password": "123456"
        })
        assert_success((status, data), "审核后教师登录失败")
        client.close()


@pytest.mark.admin
class TestAdminCourses:
    """
    管理员课程管理测试类
    
    测试管理员对课程的管理功能：获取列表、审核课程、下架课程。
    """

    def test_admin_get_courses(self, base_url, admin_token):
        """
        测试获取课程列表
        
        验证管理员可以获取所有课程的列表。
        """
        client = ApiClient(base_url, admin_token)
        status, data = client.get("/admin/courses")
        assert_success((status, data), "获取课程列表失败")
        assert isinstance(data["data"], list), "课程列表应为数组"
        client.close()

    @pytest.mark.parametrize("status_filter", ["PENDING", "PUBLISHED", "UNPUBLISHED"])
    def test_admin_get_courses_by_status(self, base_url, admin_token, status_filter):
        """
        参数化测试：按状态筛选课程
        
        验证管理员可以按不同状态筛选课程。
        
        Args:
            status_filter: 课程状态（PENDING/PUBLISHED/UNPUBLISHED）
        """
        client = ApiClient(base_url, admin_token)
        status, data = client.get(f"/admin/courses?status={status_filter}")
        assert_success((status, data), f"获取{status_filter}课程失败")
        assert isinstance(data["data"], list), "课程列表应为数组"
        client.close()

    def test_admin_approve_course(self, base_url, admin_token, teacher_token):
        """
        测试审核课程
        
        完整流程：教师创建课程 → 管理员审核。
        """
        client = ApiClient(base_url, teacher_token)
        
        # 教师创建课程（状态为 PENDING）
        status, data = client.post("/teacher/courses", {
            "title": "测试课程",
            "subject": "语文",
            "grade": "初一",
            "price": 99.0,
            "totalHours": 20,
            "description": "测试课程描述",
            "chapters": [{"title": "第一章", "duration": 45}]
        })
        assert_success((status, data), "教师创建课程失败")
        
        course_id = data["data"]["id"]
        
        # 管理员审核课程
        client.set_token(admin_token)
        status, data = client.put(f"/admin/courses/{course_id}/approve")
        assert_success((status, data), "审核课程失败")
        client.close()

    def test_admin_unpublish_course(self, base_url, admin_token, teacher_token):
        """
        测试下架课程
        
        完整流程：教师创建课程 → 管理员审核 → 管理员下架。
        """
        client = ApiClient(base_url, teacher_token)
        
        # 教师创建课程
        status, data = client.post("/teacher/courses", {
            "title": "待下架课程",
            "subject": "数学",
            "grade": "初二",
            "price": 199.0,
            "totalHours": 30,
            "description": "测试",
            "chapters": [{"title": "第一章", "duration": 45}]
        })
        assert_success((status, data), "创建课程失败")
        
        course_id = data["data"]["id"]
        
        # 管理员审核课程
        client.set_token(admin_token)
        client.put(f"/admin/courses/{course_id}/approve")
        
        # 管理员下架课程
        status, data = client.put(f"/admin/courses/{course_id}/unpublish")
        assert_success((status, data), "下架课程失败")
        client.close()


@pytest.mark.admin
class TestAdminAuthorization:
    """
    管理员权限控制测试类
    
    验证管理员接口的访问权限控制：
    - 非管理员不能访问管理员接口
    - 无 token 不能访问管理员接口
    """

    def test_admin_unauthorized_access(self, base_url, parent_token):
        """
        测试家长访问管理员接口
        
        验证家长角色不能访问管理员接口，应返回权限错误。
        """
        client = ApiClient(base_url, parent_token)
        status, data = client.get("/admin/stats")
        assert_error((status, data), "家长不应访问管理员接口")
        client.close()

    def test_admin_no_token_access(self, base_url):
        """
        测试无 token 访问管理员接口
        
        验证未登录用户不能访问管理员接口。
        """
        client = ApiClient(base_url)
        status, data = client.get("/admin/stats")
        assert_error((status, data), "无token不应访问管理员接口")
        client.close()

    @pytest.mark.parametrize("endpoint", [
        "/admin/stats",
        "/admin/users",
        "/admin/courses",
    ])
    def test_admin_endpoints_require_auth(self, base_url, endpoint):
        """
        参数化测试：管理员接口需要认证
        
        验证多个管理员接口都需要登录认证。
        
        Args:
            endpoint: 管理员接口路径
        """
        client = ApiClient(base_url)
        status, data = client.get(endpoint)
        assert_error((status, data), f"{endpoint} 需要认证")
        client.close()


@pytest.mark.admin
@pytest.mark.integration
class TestAdminIntegration:
    """
    管理员集成测试
    
    测试管理员的完整工作流程。
    """

    def test_full_admin_workflow(self, base_url, admin_token, teacher_token):
        """
        测试完整的管理员工作流程
        
        验证管理员从登录到完成课程管理的完整流程。
        """
        client = ApiClient(base_url, admin_token)
        
        # 1. 获取统计数据
        status, data = client.get("/admin/stats")
        assert_success((status, data), "获取统计失败")
        
        # 2. 获取用户列表
        status, data = client.get("/admin/users")
        assert_success((status, data), "获取用户失败")
        assert isinstance(data["data"], list)
        
        # 3. 获取课程列表
        status, data = client.get("/admin/courses")
        assert_success((status, data), "获取课程失败")
        assert isinstance(data["data"], list)
        
        # 4. 教师创建课程
        client.set_token(teacher_token)
        status, data = client.post("/teacher/courses", {
            "title": "集成测试课程",
            "subject": "英语",
            "grade": "初三",
            "price": 299.0,
            "totalHours": 40,
            "description": "集成测试",
            "chapters": [{"title": "第一章", "duration": 45}]
        })
        course_id = data["data"]["id"]
        
        # 5. 管理员审核课程
        client.set_token(admin_token)
        client.put(f"/admin/courses/{course_id}/approve")
        
        # 6. 教师删除课程（清理测试数据）
        client.set_token(teacher_token)
        client.delete(f"/teacher/courses/{course_id}")
        
        client.close()
