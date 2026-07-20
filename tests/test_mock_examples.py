"""
test_mock_examples.py - Mock 测试示例
=======================================
此文件展示了如何使用 pytest-mock 进行单元测试的 Mock 模拟。

Mock 测试适用于：
1. 测试网络请求失败场景（网络错误、超时）
2. 测试边界情况和异常处理
3. 测试代码逻辑而不需要真实服务器
4. 模拟第三方服务响应

测试分类标记：
- @pytest.mark.unit: 单元测试
- @pytest.mark.skip_server_check: 跳过服务器检查（因为是单元测试）
"""

import pytest
from unittest.mock import MagicMock, patch
from utils.api_client import ApiClient, assert_success, assert_error


@pytest.mark.unit
@pytest.mark.skip_server_check
class TestMockBasics:
    """
    Mock 基础用法示例
    
    展示如何使用 Mock 模拟 API 响应。
    """

    def test_mock_api_client_response(self, mocker):
        """
        示例：Mock ApiClient 的响应解析方法
        
        通过 patch.object 直接替换 _parse_response 方法，返回预设的模拟数据。
        """
        # 创建模拟响应数据
        mock_response = MagicMock()
        mock_response.status_code = 200
        mock_response.json.return_value = {
            "code": 200,
            "message": "success",
            "data": {"token": "mocked_token", "role": "PARENT"}
        }
        
        # 使用 patch.object 临时替换 _parse_response 方法
        with patch.object(ApiClient, '_parse_response', return_value=mock_response.json()):
            client = ApiClient("http://mock-url")
            # 调用 post 方法，但实际不会发送真实请求
            # 因为 _parse_response 被替换了，返回预设的数据
            status, data = client.post("/auth/login", {"phone": "13800138000", "password": "123456"})
            
            # 验证返回的是模拟数据
            assert status == 200
            assert data["code"] == 200
            assert data["data"]["token"] == "mocked_token"
        
        client.close()

    def test_mock_requests_post(self, mocker):
        """
        示例：Mock requests.Session.post 方法
        
        通过 mocker.patch 模拟底层的 HTTP 请求，验证请求参数是否正确。
        """
        # 创建模拟的 post 方法
        mock_post = mocker.patch('requests.Session.post')
        
        # 设置模拟响应
        mock_response = MagicMock()
        mock_response.status_code = 200
        mock_response.json.return_value = {
            "code": 200,
            "message": "success",
            "data": {"token": "mocked_token"}
        }
        mock_post.return_value = mock_response
        
        # 创建客户端并发送请求
        client = ApiClient("http://mock-url")
        status, data = client.post("/auth/login", {"phone": "13800138000", "password": "123456"})
        
        # 验证 post 方法被正确调用
        mock_post.assert_called_once_with(
            "http://mock-url/auth/login",
            json={"phone": "13800138000", "password": "123456"}
        )
        
        # 验证响应数据
        assert status == 200
        assert data["data"]["token"] == "mocked_token"
        
        client.close()


@pytest.mark.unit
@pytest.mark.skip_server_check
class TestMockErrorHandling:
    """
    Mock 错误处理示例
    
    展示如何模拟各种错误场景。
    """

    def test_mock_network_error(self, mocker):
        """
        示例：模拟网络连接错误
        
        验证当网络不可用时，代码能正确处理异常。
        """
        # 设置模拟的 post 方法抛出 ConnectionError
        mocker.patch('requests.Session.post', side_effect=ConnectionError("网络连接失败"))
        
        client = ApiClient("http://mock-url")
        # 验证抛出预期的异常
        with pytest.raises(ConnectionError):
            client.post("/auth/login", {"phone": "13800138000", "password": "123456"})
        
        client.close()

    def test_mock_timeout_error(self, mocker):
        """
        示例：模拟请求超时错误
        
        验证当请求超时时，代码能正确处理异常。
        """
        from requests.exceptions import Timeout
        
        # 设置模拟的 post 方法抛出 Timeout
        mocker.patch('requests.Session.post', side_effect=Timeout("请求超时"))
        
        client = ApiClient("http://mock-url")
        # 验证抛出预期的异常
        with pytest.raises(Timeout):
            client.post("/auth/login", {"phone": "13800138000", "password": "123456"}, timeout=1)
        
        client.close()

    def test_mock_json_decode_error(self, mocker):
        """
        示例：模拟 JSON 解析错误
        
        验证当响应不是有效的 JSON 时，代码能正确处理。
        """
        # 创建模拟响应，但 json() 方法抛出异常
        mock_response = MagicMock()
        mock_response.status_code = 200
        mock_response.text = "Not a JSON response"
        mock_response.json.side_effect = ValueError("Invalid JSON")
        mocker.patch('requests.Session.post', return_value=mock_response)
        
        client = ApiClient("http://mock-url")
        status, data = client.post("/auth/login", {"phone": "13800138000", "password": "123456"})
        
        # 验证返回的是错误处理后的结果
        assert status == 200
        assert "error" in data, "应返回错误信息"
        
        client.close()


@pytest.mark.unit
@pytest.mark.skip_server_check
class TestMockAuthentication:
    """
    Mock 认证场景示例
    
    展示如何模拟登录成功和失败的场景。
    """

    def test_mock_admin_login_success(self, mocker):
        """
        示例：模拟管理员登录成功
        
        验证登录成功后的断言逻辑。
        """
        # 设置模拟响应
        mock_response = MagicMock()
        mock_response.status_code = 200
        mock_response.json.return_value = {
            "code": 200,
            "message": "登录成功",
            "data": {
                "token": "admin_token_123",
                "role": "ADMIN",
                "userId": 1
            }
        }
        mocker.patch('requests.Session.post', return_value=mock_response)
        
        # 创建客户端并登录
        client = ApiClient("http://mock-url")
        status, data = client.post("/auth/login", {
            "phone": "13800138000",
            "password": "123456"
        })
        
        # 使用断言工具验证
        assert_success((status, data), "管理员登录失败")
        assert data["data"]["role"] == "ADMIN"
        assert data["data"]["token"] == "admin_token_123"
        
        client.close()

    def test_mock_login_failure(self, mocker):
        """
        示例：模拟登录失败
        
        验证登录失败后的断言逻辑。
        """
        # 设置模拟响应（401 未授权）
        mock_response = MagicMock()
        mock_response.status_code = 401
        mock_response.json.return_value = {
            "code": 401,
            "message": "用户名或密码错误"
        }
        mocker.patch('requests.Session.post', return_value=mock_response)
        
        # 创建客户端并登录
        client = ApiClient("http://mock-url")
        status, data = client.post("/auth/login", {
            "phone": "13800138000",
            "password": "wrong_password"
        })
        
        # 使用断言工具验证
        assert_error((status, data), expected_http=401)
        assert data["message"] == "用户名或密码错误"
        
        client.close()


@pytest.mark.unit
@pytest.mark.skip_server_check
class TestMockAuthorization:
    """
    Mock 权限控制示例
    
    展示如何模拟权限验证失败的场景。
    """

    def test_mock_unauthorized_access(self, mocker):
        """
        示例：模拟无权访问
        
        验证非管理员访问管理员接口时返回权限错误。
        """
        # 设置模拟响应（403 禁止访问）
        mock_response = MagicMock()
        mock_response.status_code = 403
        mock_response.json.return_value = {
            "code": 403,
            "message": "无权访问"
        }
        mocker.patch('requests.Session.get', return_value=mock_response)
        
        # 创建带无效 token 的客户端
        client = ApiClient("http://mock-url", "invalid_token")
        status, data = client.get("/admin/stats")
        
        # 验证权限错误
        assert_error((status, data), expected_http=403)
        assert data["message"] == "无权访问"
        
        client.close()

    def test_mock_token_expired(self, mocker):
        """
        示例：模拟 token 过期
        
        验证过期 token 访问受保护接口时返回错误。
        """
        # 设置模拟响应（401 token 过期）
        mock_response = MagicMock()
        mock_response.status_code = 401
        mock_response.json.return_value = {
            "code": 401,
            "message": "token已过期"
        }
        mocker.patch('requests.Session.get', return_value=mock_response)
        
        # 创建带过期 token 的客户端
        client = ApiClient("http://mock-url", "expired_token")
        status, data = client.get("/auth/me")
        
        # 验证 token 过期错误
        assert_error((status, data), expected_http=401)
        assert data["message"] == "token已过期"
        
        client.close()


@pytest.mark.unit
@pytest.mark.skip_server_check
class TestMockDataValidation:
    """
    Mock 数据验证示例
    
    使用参数化测试模拟各种错误响应。
    """

    @pytest.mark.parametrize("status_code,api_code,message", [
        (400, 400, "参数校验失败"),
        (404, 404, "资源不存在"),
        (500, 500, "服务器内部错误"),
    ])
    def test_mock_various_errors(self, mocker, status_code, api_code, message):
        """
        参数化示例：模拟各种错误状态码
        
        验证代码能正确处理不同类型的错误响应。
        
        Args:
            status_code: HTTP 状态码
            api_code: API 自定义错误码
            message: 错误信息
        """
        # 设置模拟响应
        mock_response = MagicMock()
        mock_response.status_code = status_code
        mock_response.json.return_value = {
            "code": api_code,
            "message": message
        }
        mocker.patch('requests.Session.post', return_value=mock_response)
        
        # 创建客户端并发送请求
        client = ApiClient("http://mock-url")
        status, data = client.post("/auth/login", {"phone": "13800138000", "password": "123456"})
        
        # 验证错误响应
        assert status == status_code
        assert data["code"] == api_code
        assert data["message"] == message
        
        client.close()


@pytest.mark.unit
@pytest.mark.skip_server_check
class TestMockChainedCalls:
    """
    Mock 链式调用示例
    
    展示如何模拟多个连续的 API 调用。
    """

    def test_mock_multiple_requests(self, mocker):
        """
        示例：模拟多个连续请求
        
        验证注册流程中多个 API 调用的顺序和参数。
        """
        # 创建多个模拟响应
        responses = [
            MagicMock(status_code=200),  # 发送验证码响应
            MagicMock(status_code=200),  # 注册响应
        ]
        responses[0].json.return_value = {
            "code": 200,
            "message": "验证码发送成功",
            "data": {}
        }
        responses[1].json.return_value = {
            "code": 200,
            "message": "注册成功",
            "data": {"userId": 100}
        }
        
        # 使用 side_effect 按顺序返回不同的响应
        mock_post = mocker.patch('requests.Session.post', side_effect=responses)
        
        # 执行注册流程
        client = ApiClient("http://mock-url")
        client.post("/auth/send-code", {"phone": "13900139000"})
        status, data = client.post("/auth/register", {
            "phone": "13900139000",
            "password": "123456",
            "realName": "测试用户",
            "role": "PARENT",
            "code": "123456"
        })
        
        # 验证调用次数和结果
        assert mock_post.call_count == 2, "应调用两次 post"
        assert_success((status, data), "注册失败")
        
        client.close()
