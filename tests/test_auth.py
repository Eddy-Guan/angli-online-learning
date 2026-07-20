"""
test_auth.py - 认证模块测试
============================
此文件包含用户认证相关的所有测试用例，包括：
1. 登录测试（正常/异常/边界值）
2. 注册测试（正常/重复/参数校验）
3. 验证码测试
4. 认证集成测试

测试分类标记：
- @pytest.mark.auth: 认证相关测试
- @pytest.mark.smoke: 冒烟测试（核心功能）
- @pytest.mark.integration: 集成测试（完整流程）
"""

import pytest
from utils.api_client import ApiClient, assert_success, assert_error


@pytest.mark.auth
@pytest.mark.smoke
class TestAuthSmoke:
    """
    认证冒烟测试
    
    验证最核心的认证功能是否正常工作。
    """

    def test_admin_login_success(self, base_url):
        """
        测试管理员登录成功
        
        验证预设的管理员账号可以正常登录，返回包含 token、role、userId 的响应。
        """
        client = ApiClient(base_url)
        status, data = client.post("/auth/login", {
            "phone": "13800138000",
            "password": "123456"
        })
        assert_success((status, data), "管理员登录失败")
        assert "token" in data["data"], "响应数据缺少 token"
        assert "role" in data["data"], "响应数据缺少 role"
        assert "userId" in data["data"], "响应数据缺少 userId"
        client.close()


@pytest.mark.auth
class TestLogin:
    """
    登录测试类
    
    覆盖各种登录场景：正常登录、错误密码、无效账号、边界值测试。
    """

    @pytest.mark.parametrize("phone,password,expected_success", [
        ("13800138000", "123456", True),
        ("13900139000", "123456", True),
        ("13700137000", "123456", True),
        ("13800138000", "wrong_password", False),
        ("19999999999", "123456", False),
    ])
    def test_login_various_cases(self, base_url, phone, password, expected_success):
        """
        参数化测试：各种登录场景
        
        使用 pytest.mark.parametrize 对不同账号密码组合进行测试。
        
        Args:
            phone: 手机号
            password: 密码
            expected_success: 是否预期登录成功
        """
        client = ApiClient(base_url)
        status, data = client.post("/auth/login", {
            "phone": phone,
            "password": password
        })
        
        if expected_success:
            assert_success((status, data), f"登录失败: {phone}")
            assert "token" in data["data"], "成功登录应返回 token"
        else:
            assert_error((status, data), f"预期失败但成功: {phone}")
        client.close()

    @pytest.mark.parametrize("phone,password", [
        ("", "123456"),
        ("13800138000", ""),
        ("", ""),
        (" ", "123456"),
        ("13800138000", " "),
    ])
    def test_login_empty_or_whitespace_fields(self, base_url, phone, password):
        """
        参数化测试：空字段或空白字符登录
        
        验证后端对空值和空白字符的校验。
        
        Args:
            phone: 手机号（空或空白）
            password: 密码（空或空白）
        """
        client = ApiClient(base_url)
        status, data = client.post("/auth/login", {
            "phone": phone,
            "password": password
        })
        assert_error((status, data), f"空字段应返回失败: phone='{phone}', password='{password}'")
        client.close()

    @pytest.mark.parametrize("phone", [
        "123",
        "abc",
        "123456789",
        "12345678901",
        "138-0013-8000",
        "(138)00138000",
    ])
    def test_login_invalid_phone_format(self, base_url, phone):
        """
        参数化测试：无效手机号格式
        
        验证后端对手机号格式的校验，包括：
        - 太短（3位）
        - 非数字（abc）
        - 9位（不足11位）
        - 11位但以12开头（非法号段）
        - 带分隔符的格式
        
        Args:
            phone: 无效格式的手机号
        """
        client = ApiClient(base_url)
        status, data = client.post("/auth/login", {
            "phone": phone,
            "password": "123456"
        })
        assert_error((status, data), f"无效手机号格式应返回失败: {phone}")
        client.close()


@pytest.mark.auth
class TestRegister:
    """
    注册测试类
    
    覆盖各种注册场景：正常注册、重复注册、参数校验、验证码错误。
    """

    @pytest.mark.parametrize("role", ["PARENT", "TEACHER"])
    def test_register_success(self, base_url, role):
        """
        参数化测试：正常注册家长和教师
        
        验证不同角色的用户可以正常注册。
        注意：教师注册后状态为 PENDING，不能立即登录。
        
        Args:
            role: 用户角色（PARENT 或 TEACHER）
        """
        import random
        # 生成随机手机号，避免重复
        phone = f"1{random.randint(5,9)}{random.randint(0,9)}{random.randint(0,9)}{random.randint(0,9)}{random.randint(0,9)}{random.randint(0,9)}{random.randint(0,9)}{random.randint(0,9)}{random.randint(0,9)}{random.randint(0,9)}"
        
        client = ApiClient(base_url)
        # 发送验证码
        client.post("/auth/send-code", {"phone": phone})
        
        # 注册
        status, data = client.post("/auth/register", {
            "phone": phone,
            "password": "123456",
            "realName": f"测试{role}",
            "role": role,
            "code": "123456"
        })
        assert_success((status, data), f"{role}注册失败")
        
        # 注册后尝试登录
        status, data = client.post("/auth/login", {
            "phone": phone,
            "password": "123456"
        })
        
        # 家长注册后可直接登录，教师需要审核
        if role == "PARENT":
            assert_success((status, data), "家长注册后登录失败")
            assert data["data"]["role"] == "PARENT"
        else:
            assert_error((status, data), "教师待审核登录应失败")
            assert "审核" in data.get("message", ""), "教师待审核应提示审核中"
        
        client.close()

    def test_register_duplicate_phone(self, base_url):
        """
        测试重复手机号注册
        
        验证后端对重复手机号的校验，应返回错误。
        """
        client = ApiClient(base_url)
        client.post("/auth/send-code", {"phone": "13800138000"})
        
        status, data = client.post("/auth/register", {
            "phone": "13800138000",
            "password": "123456",
            "realName": "重复注册",
            "role": "PARENT",
            "code": "123456"
        })
        assert_error((status, data), "重复手机号应返回失败")
        client.close()

    @pytest.mark.parametrize("field,value", [
        ("phone", ""),
        ("password", ""),
        ("realName", ""),
        ("role", ""),
        ("code", ""),
        ("phone", "123"),
        ("password", "123"),
    ])
    def test_register_invalid_fields(self, base_url, field, value):
        """
        参数化测试：无效字段注册
        
        验证后端对各字段的校验：
        - 必填字段为空
        - 手机号格式无效
        - 密码太短
        
        Args:
            field: 要测试的字段名
            value: 无效值
        """
        import random
        phone = f"1{random.randint(5,9)}{random.randint(0,9)}{random.randint(0,9)}{random.randint(0,9)}{random.randint(0,9)}{random.randint(0,9)}{random.randint(0,9)}{random.randint(0,9)}{random.randint(0,9)}{random.randint(0,9)}"
        
        client = ApiClient(base_url)
        client.post("/auth/send-code", {"phone": phone})
        
        # 构建完整的注册数据，然后将指定字段设为无效值
        data = {
            "phone": phone,
            "password": "123456",
            "realName": "测试用户",
            "role": "PARENT",
            "code": "123456"
        }
        data[field] = value
        
        status, response_data = client.post("/auth/register", data)
        assert_error((status, response_data), f"无效{field}应返回失败")
        client.close()

    def test_register_invalid_code(self, base_url):
        """
        测试验证码错误注册
        
        验证后端对验证码的校验，使用错误的验证码应返回失败。
        """
        import random
        phone = f"1{random.randint(5,9)}{random.randint(0,9)}{random.randint(0,9)}{random.randint(0,9)}{random.randint(0,9)}{random.randint(0,9)}{random.randint(0,9)}{random.randint(0,9)}{random.randint(0,9)}{random.randint(0,9)}"
        
        client = ApiClient(base_url)
        client.post("/auth/send-code", {"phone": phone})
        
        status, data = client.post("/auth/register", {
            "phone": phone,
            "password": "123456",
            "realName": "验证码错误",
            "role": "PARENT",
            "code": "999999"
        })
        assert_error((status, data), "错误验证码应返回失败")
        client.close()


@pytest.mark.auth
class TestSendCode:
    """
    验证码发送测试类
    
    覆盖验证码发送的正常和异常场景。
    """

    @pytest.mark.parametrize("phone,expected_success", [
        ("13800138000", True),
        ("13900139000", True),
        ("123", False),
        ("", False),
        ("abc", False),
    ])
    def test_send_code_various_cases(self, base_url, phone, expected_success):
        """
        参数化测试：验证码发送场景
        
        验证：
        - 有效手机号可以发送验证码
        - 无效手机号发送验证码失败
        
        Args:
            phone: 手机号
            expected_success: 是否预期发送成功
        """
        client = ApiClient(base_url)
        status, data = client.post("/auth/send-code", {"phone": phone})
        
        if expected_success:
            assert_success((status, data), f"发送验证码失败: {phone}")
        else:
            assert_error((status, data), f"无效手机号发送验证码应失败: {phone}")
        client.close()


@pytest.mark.auth
@pytest.mark.integration
class TestAuthIntegration:
    """
    认证集成测试
    
    测试完整的认证流程：发送验证码 → 注册 → 登录 → 获取用户信息。
    """

    def test_full_register_login_workflow(self, base_url):
        """
        测试完整的注册登录流程
        
        验证用户从注册到获取个人信息的完整链路。
        """
        import random
        phone = f"1{random.randint(5,9)}{random.randint(0,9)}{random.randint(0,9)}{random.randint(0,9)}{random.randint(0,9)}{random.randint(0,9)}{random.randint(0,9)}{random.randint(0,9)}{random.randint(0,9)}{random.randint(0,9)}"
        
        client = ApiClient(base_url)
        
        # 1. 发送验证码
        status, data = client.post("/auth/send-code", {"phone": phone})
        assert_success((status, data), "发送验证码失败")
        
        # 2. 注册
        status, data = client.post("/auth/register", {
            "phone": phone,
            "password": "123456",
            "realName": "集成测试用户",
            "role": "PARENT",
            "code": "123456"
        })
        assert_success((status, data), "注册失败")
        
        # 3. 登录
        status, data = client.post("/auth/login", {
            "phone": phone,
            "password": "123456"
        })
        assert_success((status, data), "登录失败")
        assert data["data"]["role"] == "PARENT"
        
        # 4. 获取用户信息（需要 token）
        token = data["data"]["token"]
        client.set_token(token)
        
        status, data = client.get("/auth/me")
        assert_success((status, data), "获取用户信息失败")
        assert data["data"]["phone"] == phone
        
        client.close()
