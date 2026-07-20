"""
test_order.py - 订单模块测试
=============================
此文件包含订单相关的所有测试用例，包括：
1. 获取订单列表
2. 创建订单
3. 获取订单详情
4. 支付订单
5. 取消订单
6. 参数校验测试
7. 订单集成测试

测试分类标记：
- @pytest.mark.order: 订单接口测试
- @pytest.mark.smoke: 冒烟测试（核心功能）
- @pytest.mark.integration: 集成测试（完整流程）
"""

import pytest
from utils.api_client import ApiClient, assert_success, assert_error


@pytest.mark.order
@pytest.mark.smoke
class TestOrderSmoke:
    """
    订单冒烟测试
    
    验证订单最核心的功能是否正常工作。
    """

    def test_get_order_list(self, base_url, parent_token):
        """
        测试获取订单列表
        
        验证家长可以获取自己的订单列表。
        """
        client = ApiClient(base_url, parent_token)
        status, data = client.get("/orders")
        assert_success((status, data), "获取订单列表失败")
        assert isinstance(data["data"], list), "订单列表应为数组"
        client.close()


@pytest.mark.order
class TestOrderManagement:
    """
    订单管理测试类
    
    测试订单的各种操作：创建、详情、支付、取消。
    """

    def test_create_order(self, base_url, parent_token, teacher_token, admin_token):
        """
        测试创建订单
        
        完整流程：教师创建课程 → 管理员审核 → 家长创建订单。
        """
        client = ApiClient(base_url, teacher_token)
        
        # 1. 教师创建课程
        status, data = client.post("/teacher/courses", {
            "title": "订单测试课程",
            "subject": "数学",
            "grade": "初一",
            "price": 99.0,
            "totalHours": 20,
            "description": "测试",
            "chapters": [{"title": "第一章", "duration": 45}]
        })
        course_id = data["data"]["id"]
        
        # 2. 管理员审核课程（否则无法购买）
        client.set_token(admin_token)
        client.put(f"/admin/courses/{course_id}/approve")
        
        # 3. 家长创建订单
        client.set_token(parent_token)
        status, data = client.post("/orders", {"courseId": course_id})
        assert_success((status, data), "创建订单失败")
        assert data["data"]["courseId"] == course_id, "订单的课程ID不匹配"
        assert data["data"]["status"] == "PENDING", "订单状态应为 PENDING"
        
        # 清理测试数据
        client.set_token(teacher_token)
        client.delete(f"/teacher/courses/{course_id}")
        client.close()

    def test_get_order_detail(self, base_url, parent_token):
        """
        测试获取订单详情
        
        验证家长可以获取单个订单的详细信息。
        """
        client = ApiClient(base_url, parent_token)
        status, data = client.get("/orders")
        assert_success((status, data), "获取订单列表失败")
        
        if data["data"]:
            order_id = data["data"][0]["id"]
            status, data = client.get(f"/orders/{order_id}")
            assert_success((status, data), "获取订单详情失败")
            assert data["data"]["id"] == order_id, "返回的订单ID不匹配"
        client.close()

    def test_pay_order(self, base_url, parent_token, teacher_token, admin_token):
        """
        测试支付订单
        
        完整流程：教师创建课程 → 管理员审核 → 家长创建订单 → 家长支付订单。
        """
        client = ApiClient(base_url, teacher_token)
        
        # 1. 教师创建课程
        status, data = client.post("/teacher/courses", {
            "title": "支付测试课程",
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
        
        # 3. 家长创建订单
        client.set_token(parent_token)
        status, data = client.post("/orders", {"courseId": course_id})
        order_id = data["data"]["id"]
        
        # 4. 家长支付订单
        status, data = client.put(f"/orders/{order_id}/pay")
        assert_success((status, data), "支付订单失败")
        assert data["data"]["status"] == "PAID", "订单状态应为 PAID"
        
        # 清理测试数据
        client.set_token(teacher_token)
        client.delete(f"/teacher/courses/{course_id}")
        client.close()

    def test_cancel_order(self, base_url, parent_token, teacher_token, admin_token):
        """
        测试取消订单
        
        完整流程：教师创建课程 → 管理员审核 → 家长创建订单 → 家长取消订单。
        """
        client = ApiClient(base_url, teacher_token)
        
        # 1. 教师创建课程
        status, data = client.post("/teacher/courses", {
            "title": "取消测试课程",
            "subject": "英语",
            "grade": "初三",
            "price": 299.0,
            "totalHours": 40,
            "description": "测试",
            "chapters": [{"title": "第一章", "duration": 45}]
        })
        course_id = data["data"]["id"]
        
        # 2. 管理员审核课程
        client.set_token(admin_token)
        client.put(f"/admin/courses/{course_id}/approve")
        
        # 3. 家长创建订单
        client.set_token(parent_token)
        status, data = client.post("/orders", {"courseId": course_id})
        order_id = data["data"]["id"]
        
        # 4. 家长取消订单
        status, data = client.put(f"/orders/{order_id}/cancel")
        assert_success((status, data), "取消订单失败")
        assert data["data"]["status"] == "CANCELLED", "订单状态应为 CANCELLED"
        
        # 清理测试数据
        client.set_token(teacher_token)
        client.delete(f"/teacher/courses/{course_id}")
        client.close()


@pytest.mark.order
class TestOrderValidation:
    """
    订单参数校验测试类
    
    测试订单相关的参数校验。
    """

    def test_create_order_without_course(self, base_url, parent_token):
        """
        测试创建订单时课程不存在
        
        验证使用无效课程ID创建订单应返回失败。
        """
        client = ApiClient(base_url, parent_token)
        status, data = client.post("/orders", {"courseId": 999999})
        assert_error((status, data), "无效课程ID应返回失败")
        client.close()


@pytest.mark.order
@pytest.mark.integration
class TestOrderIntegration:
    """
    订单集成测试
    
    测试订单的完整生命周期：创建 → 支付 → 清理。
    """

    def test_full_order_workflow(self, base_url, parent_token, teacher_token, admin_token):
        """
        测试完整的订单工作流程
        
        验证订单从创建到完成的完整生命周期。
        """
        client = ApiClient(base_url, teacher_token)
        
        # 1. 教师创建课程
        status, data = client.post("/teacher/courses", {
            "title": "订单集成测试",
            "subject": "物理",
            "grade": "高一",
            "price": 399.0,
            "totalHours": 50,
            "description": "集成测试",
            "chapters": [{"title": "第一章", "duration": 45}]
        })
        course_id = data["data"]["id"]
        
        # 2. 管理员审核课程
        client.set_token(admin_token)
        client.put(f"/admin/courses/{course_id}/approve")
        
        # 3. 家长创建订单
        client.set_token(parent_token)
        status, data = client.post("/orders", {"courseId": course_id})
        order_id = data["data"]["id"]
        
        # 4. 家长支付订单
        client.put(f"/orders/{order_id}/pay")
        
        # 5. 清理测试数据（删除课程会级联删除订单）
        client.set_token(teacher_token)
        client.delete(f"/teacher/courses/{course_id}")
        client.close()
