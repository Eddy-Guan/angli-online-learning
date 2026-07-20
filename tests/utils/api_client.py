"""
api_client.py - API 客户端封装模块
====================================
此模块提供了统一的 API 请求封装和断言工具，用于简化测试代码编写。

核心组件：
1. ApiClient 类 - HTTP 请求封装，支持 GET/POST/PUT/DELETE
2. 断言工具函数 - assert_success/assert_error/assert_response_contains
3. 便捷函数 - create_api_client/login/register
"""

import requests
import json
import logging
from typing import Any, Dict, Optional, Tuple, Union

# 配置日志，便于调试请求和响应
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


class ApiClient:
    """
    API 客户端类
    
    封装了 requests.Session，提供统一的 HTTP 请求接口。
    自动处理 JSON 序列化/反序列化、请求日志和错误处理。
    
    使用示例：
        # 创建客户端
        client = ApiClient("http://localhost:8080/api")
        
        # 设置认证 token
        client.set_token("jwt_token_here")
        
        # 发送请求
        status, data = client.post("/auth/login", {"phone": "13800138000", "password": "123456"})
        
        # 清理
        client.close()
    
    Args:
        base_url: API 基础地址（如 http://localhost:8080/api）
        token: 可选的 JWT token，用于认证
    """
    
    def __init__(self, base_url: str, token: Optional[str] = None):
        """
        初始化 API 客户端
        
        Args:
            base_url: API 基础地址
            token: JWT token（可选）
        """
        self.base_url = base_url
        self.session = requests.Session()
        self.session.headers.update({"Content-Type": "application/json"})
        if token:
            self.set_token(token)

    def set_token(self, token: str):
        """
        设置认证 Token
        
        将 JWT token 添加到请求头的 Authorization 字段。
        
        Args:
            token: JWT token 字符串
        """
        self.session.headers.update({"Authorization": f"Bearer {token}"})

    def clear_token(self):
        """
        清除认证 Token
        
        从请求头中移除 Authorization 字段。
        """
        self.session.headers.pop("Authorization", None)

    def get(self, endpoint: str, **kwargs) -> Tuple[int, Dict[str, Any]]:
        """
        发送 GET 请求
        
        Args:
            endpoint: API 端点（如 /courses）
            **kwargs: 传递给 requests.Session.get 的额外参数
        
        Returns:
            Tuple[int, Dict]: (HTTP状态码, 响应数据字典)
        """
        url = f"{self.base_url}{endpoint}"
        logger.info(f"GET {url}")
        response = self.session.get(url, **kwargs)
        logger.info(f"Response: {response.status_code}")
        return response.status_code, self._parse_response(response)

    def post(self, endpoint: str, data: Optional[Dict[str, Any]] = None, **kwargs) -> Tuple[int, Dict[str, Any]]:
        """
        发送 POST 请求
        
        Args:
            endpoint: API 端点（如 /auth/login）
            data: 请求体数据（会自动序列化为 JSON）
            **kwargs: 传递给 requests.Session.post 的额外参数
        
        Returns:
            Tuple[int, Dict]: (HTTP状态码, 响应数据字典)
        """
        url = f"{self.base_url}{endpoint}"
        logger.info(f"POST {url}")
        if data:
            logger.debug(f"Data: {json.dumps(data, ensure_ascii=False)}")
        response = self.session.post(url, json=data, **kwargs)
        logger.info(f"Response: {response.status_code}")
        return response.status_code, self._parse_response(response)

    def put(self, endpoint: str, data: Optional[Dict[str, Any]] = None, **kwargs) -> Tuple[int, Dict[str, Any]]:
        """
        发送 PUT 请求
        
        Args:
            endpoint: API 端点（如 /teacher/courses/1）
            data: 请求体数据（会自动序列化为 JSON）
            **kwargs: 传递给 requests.Session.put 的额外参数
        
        Returns:
            Tuple[int, Dict]: (HTTP状态码, 响应数据字典)
        """
        url = f"{self.base_url}{endpoint}"
        logger.info(f"PUT {url}")
        if data:
            logger.debug(f"Data: {json.dumps(data, ensure_ascii=False)}")
        response = self.session.put(url, json=data, **kwargs)
        logger.info(f"Response: {response.status_code}")
        return response.status_code, self._parse_response(response)

    def delete(self, endpoint: str, **kwargs) -> Tuple[int, Dict[str, Any]]:
        """
        发送 DELETE 请求
        
        Args:
            endpoint: API 端点（如 /teacher/courses/1）
            **kwargs: 传递给 requests.Session.delete 的额外参数
        
        Returns:
            Tuple[int, Dict]: (HTTP状态码, 响应数据字典)
        """
        url = f"{self.base_url}{endpoint}"
        logger.info(f"DELETE {url}")
        response = self.session.delete(url, **kwargs)
        logger.info(f"Response: {response.status_code}")
        return response.status_code, self._parse_response(response)

    def _parse_response(self, response: requests.Response) -> Dict[str, Any]:
        """
        解析 HTTP 响应
        
        尝试将响应解析为 JSON，如果失败则返回错误信息。
        
        Args:
            response: requests.Response 对象
        
        Returns:
            Dict: 解析后的响应数据
        """
        try:
            return response.json()
        except json.JSONDecodeError:
            logger.warning(f"Failed to parse JSON response: {response.text}")
            return {"error": response.text, "status_code": response.status_code}

    def close(self):
        """
        关闭客户端会话
        
        释放网络连接资源。
        """
        self.session.close()


# ==================== 断言工具函数 ====================

def assert_success(response: Tuple[int, Dict[str, Any]], msg: str = ""):
    """
    断言请求成功
    
    验证 HTTP 状态码为 200 且 API 返回码为 200。
    
    Args:
        response: ApiClient 返回的 (status_code, data) 元组
        msg: 错误提示信息（可选）
    
    Raises:
        AssertionError: 当请求失败时抛出
    """
    status_code, data = response
    assert status_code == 200, f"{msg} - HTTP status: {status_code}"
    assert data.get("code") == 200, f"{msg} - API code: {data.get('code')}, message: {data.get('message')}"


def assert_error(response: Tuple[int, Dict[str, Any]], expected_http: int = None, expected_api: int = None, msg: str = ""):
    """
    断言请求失败
    
    验证 API 返回码不为 200，可以指定预期的 HTTP 状态码和 API 返回码。
    
    Args:
        response: ApiClient 返回的 (status_code, data) 元组
        expected_http: 预期的 HTTP 状态码（可选）
        expected_api: 预期的 API 返回码（可选）
        msg: 错误提示信息（可选）
    
    Raises:
        AssertionError: 当请求意外成功或状态码不匹配时抛出
    """
    status_code, data = response
    if expected_http:
        assert status_code == expected_http, f"{msg} - HTTP status: {status_code}"
    if expected_api:
        assert data.get("code") == expected_api, f"{msg} - API code: {data.get('code')}"
    assert data.get("code") != 200, f"{msg} - Expected error but got success"


def assert_response_contains(response: Tuple[int, Dict[str, Any]], *keys: str):
    """
    断言响应数据包含指定字段
    
    验证响应的 data 对象中包含所有指定的键。
    
    Args:
        response: ApiClient 返回的 (status_code, data) 元组
        *keys: 需要验证存在的字段名
    
    Raises:
        AssertionError: 当缺少字段时抛出
    """
    _, data = response
    for key in keys:
        assert key in data["data"], f"Response data missing key: {key}"


# ==================== 便捷函数 ====================

def create_api_client(base_url: str, token: Optional[str] = None) -> ApiClient:
    """
    创建 API 客户端的便捷函数
    
    等同于直接实例化 ApiClient。
    
    Args:
        base_url: API 基础地址
        token: JWT token（可选）
    
    Returns:
        ApiClient: 创建的客户端实例
    """
    return ApiClient(base_url, token)


def login(client: ApiClient, phone: str, password: str) -> str:
    """
    登录并返回 token
    
    封装登录流程，自动断言登录成功。
    
    Args:
        client: ApiClient 实例
        phone: 手机号
        password: 密码
    
    Returns:
        str: JWT token
    """
    status, data = client.post("/auth/login", {"phone": phone, "password": password})
    assert_success((status, data), "Login failed")
    return data["data"]["token"]


def register(client: ApiClient, phone: str, password: str, real_name: str, role: str, code: str = "123456") -> str:
    """
    注册新用户并返回 token
    
    完整流程：发送验证码 → 注册 → 登录。
    自动断言每一步成功。
    
    Args:
        client: ApiClient 实例
        phone: 手机号
        password: 密码
        real_name: 真实姓名
        role: 用户角色（PARENT/TEACHER）
        code: 验证码（默认 123456）
    
    Returns:
        str: 登录后的 JWT token
    """
    client.post("/auth/send-code", {"phone": phone})
    status, data = client.post("/auth/register", {
        "phone": phone, "password": password,
        "realName": real_name, "role": role, "code": code
    })
    assert_success((status, data), "Register failed")
    return login(client, phone, password)
