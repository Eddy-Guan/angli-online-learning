"""
test_checkin.py - 签到模块测试
==============================
此文件包含签到相关的所有测试用例，包括：
1. 用户签到
2. 获取签到历史
3. 获取今日签到状态
4. 重复签到测试
5. 获取月度签到统计
6. 管理员获取签到列表

测试分类标记：
- @pytest.mark.checkin: 签到接口测试
"""

import pytest
from utils.api_client import ApiClient, assert_success, assert_error


@pytest.mark.checkin
class TestCheckin:
    """
    签到测试类
    
    测试签到功能的各种操作：签到、历史、今日状态、月度统计。
    """

    def test_checkin(self, base_url, parent_token):
        """
        测试用户签到
        
        验证用户可以正常签到，返回签到成功状态。
        """
        client = ApiClient(base_url, parent_token)
        status, data = client.post("/checkin")
        assert_success((status, data), "签到失败")
        assert data["data"]["success"] is True, "签到应返回成功"
        client.close()

    def test_get_checkin_history(self, base_url, parent_token):
        """
        测试获取签到历史
        
        验证用户可以获取自己的签到历史记录。
        """
        client = ApiClient(base_url, parent_token)
        status, data = client.get("/checkin/history")
        assert_success((status, data), "获取签到历史失败")
        assert isinstance(data["data"], list), "签到历史应为数组"
        client.close()

    def test_get_checkin_today(self, base_url, parent_token):
        """
        测试获取今日签到状态
        
        验证用户可以获取今日的签到状态（已签到/未签到）。
        """
        client = ApiClient(base_url, parent_token)
        status, data = client.get("/checkin/today")
        assert_success((status, data), "获取今日签到状态失败")
        client.close()

    def test_checkin_twice(self, base_url, parent_token):
        """
        测试重复签到
        
        验证用户一天只能签到一次，重复签到应返回失败。
        """
        client = ApiClient(base_url, parent_token)
        
        # 第一次签到
        status, data = client.post("/checkin")
        assert_success((status, data), "第一次签到失败")
        
        # 第二次签到（应失败）
        status, data = client.post("/checkin")
        assert_error((status, data), "重复签到应返回失败")
        client.close()

    def test_get_month_stats(self, base_url, parent_token):
        """
        测试获取月度签到统计
        
        验证用户可以获取当月的签到统计数据：
        - 签到次数
        - 连续签到天数
        """
        client = ApiClient(base_url, parent_token)
        status, data = client.get("/checkin/month-stats")
        assert_success((status, data), "获取月度签到统计失败")
        assert "checkinCount" in data["data"], "响应缺少 checkinCount"
        assert "continuousDays" in data["data"], "响应缺少 continuousDays"
        client.close()

    def test_get_checkin_list(self, base_url, admin_token):
        """
        测试管理员获取签到列表
        
        验证管理员可以获取所有用户的签到记录列表。
        """
        client = ApiClient(base_url, admin_token)
        status, data = client.get("/admin/checkin")
        assert_success((status, data), "管理员获取签到列表失败")
        assert isinstance(data["data"], list), "签到列表应为数组"
        client.close()
