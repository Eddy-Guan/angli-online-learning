"""
conftest.py - pytest 全局配置文件
====================================
此文件包含整个测试套件的核心 fixture 和配置，遵循 pytest-patterns 最佳实践。

主要功能：
1. 环境变量加载与配置
2. 认证 token fixture（管理员/家长/教师）
3. API 客户端工厂 fixture
4. 测试数据工厂（用户/课程/学生/订单）
5. 参数化 fixture（多角色/多状态）
6. pytest 钩子函数（标记注册/报告生成）
"""

import pytest
import requests
import os
import logging
import random
from dotenv import load_dotenv
from typing import Callable, Optional, Dict, Any, Generator, TYPE_CHECKING

if TYPE_CHECKING:
    import pytest_html

# 加载 .env 文件中的环境变量
load_dotenv()

# 从环境变量读取配置，提供默认值作为 fallback
BASE_URL = os.getenv("API_BASE_URL", "http://localhost:8080/api")
TEST_ADMIN_PHONE = os.getenv("TEST_ADMIN_PHONE", "13800138000")
TEST_ADMIN_PASSWORD = os.getenv("TEST_ADMIN_PASSWORD", "123456")
TEST_PARENT_PHONE = os.getenv("TEST_PARENT_PHONE", "13900139000")
TEST_PARENT_PASSWORD = os.getenv("TEST_PARENT_PASSWORD", "123456")
TEST_TEACHER_PHONE = os.getenv("TEST_TEACHER_PHONE", "13700137000")
TEST_TEACHER_PASSWORD = os.getenv("TEST_TEACHER_PASSWORD", "123456")
TEST_CODE = os.getenv("TEST_CODE", "123456")

# 配置日志输出，便于测试调试和问题定位
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


# ==================== 基础 Fixture ====================

@pytest.fixture(scope="session")
def base_url() -> str:
    """
    返回 API 基础地址
    
    Scope: session - 整个测试会话只执行一次
    """
    return BASE_URL


@pytest.fixture(scope="session")
def session_client() -> Generator[requests.Session, None, None]:
    """
    创建全局 requests.Session 对象
    
    使用 Session 可以复用 TCP 连接，提升测试性能。
    默认设置 Content-Type 为 application/json。
    
    Scope: session - 整个测试会话共享一个 Session
    """
    session = requests.Session()
    session.headers.update({"Content-Type": "application/json"})
    yield session
    session.close()


# ==================== 认证 Token Fixture ====================

@pytest.fixture(scope="module")
def admin_token(session_client: requests.Session, base_url: str) -> Generator[str, None, None]:
    """
    获取管理员认证 Token
    
    使用预设的管理员账号登录，返回 JWT token。
    如果登录失败会抛出断言错误。
    
    Scope: module - 每个测试模块只获取一次 token
    
    Args:
        session_client: 全局 Session 对象
        base_url: API 基础地址
    
    Returns:
        str: JWT token 字符串
    """
    response = session_client.post(
        f"{base_url}/auth/login",
        json={"phone": TEST_ADMIN_PHONE, "password": TEST_ADMIN_PASSWORD}
    )
    assert response.status_code == 200, "管理员登录 HTTP 请求失败"
    data = response.json()
    assert data["code"] == 200, f"管理员登录 API 返回错误: {data.get('message')}"
    return data["data"]["token"]


@pytest.fixture(scope="module")
def parent_token(session_client: requests.Session, base_url: str) -> Generator[str, None, None]:
    """
    获取家长认证 Token
    
    优先尝试登录，如果账号不存在则先注册再登录。
    家长注册后直接可用，无需审核。
    
    Scope: module - 每个测试模块只获取一次 token
    
    Args:
        session_client: 全局 Session 对象
        base_url: API 基础地址
    
    Returns:
        str: JWT token 字符串
    """
    # 先尝试登录
    response = session_client.post(
        f"{base_url}/auth/login",
        json={"phone": TEST_PARENT_PHONE, "password": TEST_PARENT_PASSWORD}
    )
    if response.status_code == 200:
        data = response.json()
        if data["code"] == 200:
            return data["data"]["token"]
    
    # 登录失败则注册新账号
    session_client.post(f"{base_url}/auth/send-code", json={"phone": TEST_PARENT_PHONE})
    session_client.post(f"{base_url}/auth/register", json={
        "phone": TEST_PARENT_PHONE, "password": TEST_PARENT_PASSWORD,
        "realName": "测试家长", "role": "PARENT", "code": TEST_CODE
    })
    
    # 注册后再次登录获取 token
    response = session_client.post(
        f"{base_url}/auth/login",
        json={"phone": TEST_PARENT_PHONE, "password": TEST_PARENT_PASSWORD}
    )
    return response.json()["data"]["token"]


@pytest.fixture(scope="module")
def teacher_token(session_client: requests.Session, base_url: str, admin_token: str) -> Generator[str, None, None]:
    """
    获取教师认证 Token
    
    流程：尝试登录 → 不存在则注册 → 管理员审核 → 登录获取 token
    教师账号注册后状态为 PENDING，必须经过管理员审核才能登录。
    
    Scope: module - 每个测试模块只获取一次 token
    
    Args:
        session_client: 全局 Session 对象
        base_url: API 基础地址
        admin_token: 管理员 token（用于审核教师）
    
    Returns:
        str: JWT token 字符串
    """
    # 先尝试登录
    response = session_client.post(
        f"{base_url}/auth/login",
        json={"phone": TEST_TEACHER_PHONE, "password": TEST_TEACHER_PASSWORD}
    )
    if response.status_code == 200:
        data = response.json()
        if data["code"] == 200:
            return data["data"]["token"]
    
    # 登录失败则注册新账号
    session_client.post(f"{base_url}/auth/send-code", json={"phone": TEST_TEACHER_PHONE})
    session_client.post(f"{base_url}/auth/register", json={
        "phone": TEST_TEACHER_PHONE, "password": TEST_TEACHER_PASSWORD,
        "realName": "测试教师", "role": "TEACHER", "code": TEST_CODE
    })
    
    # 教师账号需要管理员审核
    # 先查询用户获取数据库ID（后端接口使用ID而非手机号）
    response = session_client.get(
        f"{base_url}/admin/users?role=TEACHER",
        headers={"Authorization": f"Bearer {admin_token}"}
    )
    users = response.json()["data"]
    teacher_id = None
    for user in users:
        if user["phone"] == TEST_TEACHER_PHONE:
            teacher_id = user["id"]
            break
    
    if teacher_id:
        session_client.put(
            f"{base_url}/admin/users/{teacher_id}/approve",
            headers={"Authorization": f"Bearer {admin_token}"}
        )
    
    # 审核通过后登录获取 token
    response = session_client.post(
        f"{base_url}/auth/login",
        json={"phone": TEST_TEACHER_PHONE, "password": TEST_TEACHER_PASSWORD}
    )
    return response.json()["data"]["token"]


# ==================== 客户端工厂 Fixture ====================

@pytest.fixture
def api_client(base_url: str) -> Generator[Callable[[Optional[str]], requests.Session], None, None]:
    """
    API 客户端工厂 Fixture
    
    返回一个工厂函数，可以创建带或不带 token 的 Session 对象。
    所有创建的客户端会在测试结束后自动关闭，避免资源泄漏。
    
    使用示例：
        def test_example(api_client):
            client = api_client()           # 无 token 的客户端
            client = api_client(token)      # 带 token 的客户端
    
    Args:
        base_url: API 基础地址
    
    Returns:
        Callable: 工厂函数，接受可选的 token 参数，返回 Session 对象
    """
    clients = []  # 保存所有创建的客户端，用于清理
    
    def _client(token: Optional[str] = None) -> requests.Session:
        session = requests.Session()
        session.headers.update({"Content-Type": "application/json"})
        if token:
            session.headers.update({"Authorization": f"Bearer {token}"})
        clients.append(session)
        return session
    
    yield _client
    
    # 清理阶段：关闭所有创建的客户端
    for client in clients:
        client.close()


# ==================== 服务器检查 Fixture ====================

@pytest.fixture(scope="session")
def ensure_server_running(base_url: str) -> None:
    """
    确保 API 服务器正在运行
    
    在测试会话开始前检查服务器是否可达，不可达则跳过所有测试。
    
    Scope: session - 整个会话只检查一次
    
    Args:
        base_url: API 基础地址
    """
    try:
        requests.get(f"{base_url}/auth/login", timeout=3)
    except requests.exceptions.ConnectionError:
        pytest.skip(f"API server not running at {base_url}")


@pytest.fixture(autouse=True)
def skip_if_server_not_running(request: pytest.FixtureRequest, base_url: str) -> None:
    """
    自动跳过服务器未运行的测试
    
    autouse=True 表示此 fixture 会自动应用到所有测试。
    如果测试标记了 @pytest.mark.skip_server_check，则跳过此检查。
    
    Args:
        request: pytest 请求对象，用于获取测试标记
        base_url: API 基础地址
    """
    if "skip_server_check" not in request.keywords:
        try:
            requests.get(f"{base_url}/auth/login", timeout=3)
        except requests.exceptions.ConnectionError:
            pytest.skip(f"API server not running at {base_url}")


# ==================== 测试数据工厂 Fixture ====================

@pytest.fixture
def create_test_user(session_client: requests.Session, base_url: str, admin_token: str) -> Generator[Callable[[str, Optional[str]], str], None, None]:
    """
    测试用户工厂 Fixture
    
    返回一个工厂函数，用于创建临时测试用户。
    创建的用户会在测试结束后自动清理（删除）。
    
    使用示例：
        def test_example(create_test_user):
            phone = create_test_user()              # 创建家长用户
            phone = create_test_user("TEACHER")     # 创建教师用户
            phone = create_test_user("PARENT", "13812345678")  # 指定手机号
    
    Args:
        session_client: 全局 Session 对象
        base_url: API 基础地址
        admin_token: 管理员 token（用于审核教师和删除用户）
    
    Returns:
        Callable: 工厂函数，接受 role 和 phone 参数，返回创建的用户手机号
    """
    users_created = []  # 保存创建的用户，用于清理
    
    def _create_user(role: str = "PARENT", phone: Optional[str] = None) -> str:
        # 如果未指定手机号，生成随机手机号
        phone = phone or f"1{random.randint(5,9)}{''.join(str(random.randint(0,9)) for _ in range(9))}"
        
        # 发送验证码并注册
        session_client.post(f"{base_url}/auth/send-code", json={"phone": phone})
        session_client.post(f"{base_url}/auth/register", json={
            "phone": phone, "password": "123456",
            "realName": f"测试{role}", "role": role, "code": TEST_CODE
        })
        
        # 教师需要审核
        if role == "TEACHER":
            session_client.put(
                f"{base_url}/admin/users/{phone}/approve",
                headers={"Authorization": f"Bearer {admin_token}"}
            )
        
        users_created.append(phone)
        return phone
    
    yield _create_user
    
    # 清理阶段：删除所有创建的用户
    for phone in users_created:
        try:
            session_client.delete(
                f"{base_url}/admin/users/{phone}",
                headers={"Authorization": f"Bearer {admin_token}"}
            )
        except Exception:
            # 忽略删除失败（可能已被其他测试删除）
            pass


@pytest.fixture
def create_test_course(session_client: requests.Session, base_url: str, teacher_token: str) -> Generator[Callable[..., Optional[int]], None, None]:
    """
    测试课程工厂 Fixture
    
    返回一个工厂函数，用于创建临时测试课程。
    创建的课程会在测试结束后自动清理（删除）。
    
    使用示例：
        def test_example(create_test_course):
            course_id = create_test_course()                              # 默认课程
            course_id = create_test_course(title="自定义课程", price=199.0)  # 自定义参数
    
    Args:
        session_client: 全局 Session 对象
        base_url: API 基础地址
        teacher_token: 教师 token（用于创建和删除课程）
    
    Returns:
        Callable: 工厂函数，接受关键字参数，返回创建的课程 ID
    """
    courses_created = []  # 保存创建的课程，用于清理
    
    def _create_course(**kwargs) -> Optional[int]:
        # 默认课程数据，可通过 kwargs 覆盖
        data = {
            "title": "测试课程",
            "subject": "数学",
            "grade": "初一",
            "price": 99.0,
            "totalHours": 20,
            "description": "测试描述",
            "chapters": [{"title": "第一章", "duration": 45}],
            **kwargs
        }
        
        # 创建课程
        response = session_client.post(
            f"{base_url}/teacher/courses",
            json=data,
            headers={"Authorization": f"Bearer {teacher_token}"}
        )
        
        if response.status_code == 200:
            course_id = response.json()["data"]["id"]
            courses_created.append(course_id)
            return course_id
        return None
    
    yield _create_course
    
    # 清理阶段：删除所有创建的课程
    for course_id in courses_created:
        try:
            session_client.delete(
                f"{base_url}/teacher/courses/{course_id}",
                headers={"Authorization": f"Bearer {teacher_token}"}
            )
        except Exception:
            pass


@pytest.fixture
def create_test_student(session_client: requests.Session, base_url: str, parent_token: str) -> Generator[Callable[..., Optional[int]], None, None]:
    """
    测试学生（孩子）工厂 Fixture
    
    返回一个工厂函数，用于创建临时测试学生。
    创建的学生会在测试结束后自动清理（删除）。
    
    使用示例：
        def test_example(create_test_student):
            student_id = create_test_student()                              # 默认学生
            student_id = create_test_student(name="小明", grade="初二")       # 自定义参数
    
    Args:
        session_client: 全局 Session 对象
        base_url: API 基础地址
        parent_token: 家长 token（用于绑定和删除学生）
    
    Returns:
        Callable: 工厂函数，接受关键字参数，返回创建的学生 ID
    """
    students_created = []  # 保存创建的学生，用于清理
    
    def _create_student(**kwargs) -> Optional[int]:
        # 默认学生数据，可通过 kwargs 覆盖
        data = {
            "name": "测试孩子",
            "grade": "初一",
            "age": 13,
            "gender": "MALE",
            **kwargs
        }
        
        # 绑定孩子
        response = session_client.post(
            f"{base_url}/students/bind",
            json=data,
            headers={"Authorization": f"Bearer {parent_token}"}
        )
        
        if response.status_code == 200:
            student_id = response.json()["data"]["id"]
            students_created.append(student_id)
            return student_id
        return None
    
    yield _create_student
    
    # 清理阶段：删除所有创建的学生
    for student_id in students_created:
        try:
            session_client.delete(
                f"{base_url}/students/{student_id}",
                headers={"Authorization": f"Bearer {parent_token}"}
            )
        except Exception:
            pass


@pytest.fixture
def create_test_order(session_client: requests.Session, base_url: str, parent_token: str, create_test_course: Callable[..., Optional[int]], admin_token: str) -> Generator[Callable[[int], Optional[int]], None, None]:
    """
    测试订单工厂 Fixture
    
    返回一个工厂函数，用于创建临时测试订单。
    创建订单前会先审核课程（确保课程可购买）。
    
    使用示例：
        def test_example(create_test_course, create_test_order):
            course_id = create_test_course()
            order_id = create_test_order(course_id)
    
    Args:
        session_client: 全局 Session 对象
        base_url: API 基础地址
        parent_token: 家长 token（用于创建订单）
        create_test_course: 课程工厂 fixture
        admin_token: 管理员 token（用于审核课程）
    
    Returns:
        Callable: 工厂函数，接受 course_id 参数，返回创建的订单 ID
    """
    orders_created = []  # 保存创建的订单，用于清理
    
    def _create_order(course_id: int) -> Optional[int]:
        # 先审核课程（否则无法购买）
        session_client.put(
            f"{base_url}/admin/courses/{course_id}/approve",
            headers={"Authorization": f"Bearer {admin_token}"}
        )
        
        # 创建订单
        response = session_client.post(
            f"{base_url}/orders",
            json={"courseId": course_id},
            headers={"Authorization": f"Bearer {parent_token}"}
        )
        
        if response.status_code == 200:
            order_id = response.json()["data"]["id"]
            orders_created.append(order_id)
            return order_id
        return None
    
    yield _create_order
    
    # 注意：订单清理由课程清理时级联删除，此处无需额外处理


# ==================== 参数化 Fixture ====================

@pytest.fixture(params=["admin_token", "parent_token", "teacher_token"])
def any_token(request: pytest.FixtureRequest) -> str:
    """
    参数化 Token Fixture
    
    会为每个角色（管理员/家长/教师）生成一个测试用例。
    使用 request.getfixturevalue 动态获取对应的 fixture 值。
    
    使用示例：
        def test_all_roles(any_token):
            # 此测试会执行 3 次，分别使用 admin_token、parent_token、teacher_token
            client = ApiClient(base_url, any_token)
    
    Args:
        request: pytest 请求对象
    
    Returns:
        str: 当前角色的 JWT token
    """
    return request.getfixturevalue(request.param)


@pytest.fixture(params=[1, 10, 100])
def numeric_param(request: pytest.FixtureRequest) -> int:
    """
    参数化数值 Fixture
    
    提供常用的测试数值，用于边界值测试。
    
    使用示例：
        def test_with_different_values(numeric_param):
            # 此测试会执行 3 次，分别使用 1、10、100
            assert numeric_param > 0
    
    Args:
        request: pytest 请求对象
    
    Returns:
        int: 当前数值参数
    """
    return request.param


@pytest.fixture
def random_phone() -> str:
    """
    生成随机手机号 Fixture
    
    返回一个符合中国手机号格式的随机号码（1开头，共11位）。
    
    Returns:
        str: 随机手机号
    """
    return f"1{random.randint(5,9)}{''.join(str(random.randint(0,9)) for _ in range(9))}"


# ==================== pytest 钩子函数 ====================

def pytest_configure(config: pytest.Config) -> None:
    """
    pytest 配置钩子
    
    注册自定义测试标记（markers），用于分类和筛选测试。
    
    Args:
        config: pytest 配置对象
    """
    config.addinivalue_line("markers", "auth: 认证相关测试")
    config.addinivalue_line("markers", "admin: 管理员接口测试")
    config.addinivalue_line("markers", "course: 课程接口测试")
    config.addinivalue_line("markers", "teacher: 教师接口测试")
    config.addinivalue_line("markers", "student: 学生接口测试")
    config.addinivalue_line("markers", "order: 订单接口测试")
    config.addinivalue_line("markers", "evaluation: 评价接口测试")
    config.addinivalue_line("markers", "checkin: 签到接口测试")
    config.addinivalue_line("markers", "integration: 集成测试")
    config.addinivalue_line("markers", "smoke: 冒烟测试")
    config.addinivalue_line("markers", "unit: 单元测试")
    config.addinivalue_line("markers", "skip_server_check: 跳过服务器检查")


def pytest_html_report_title(report: "pytest_html.plugin.HTMLReport") -> None:
    """
    HTML 报告标题钩子
    
    自定义测试报告的标题。
    
    Args:
        report: HTML 报告对象
    """
    report.title = "昂立在线教育平台 - API测试报告"


def pytest_report_header(config: pytest.Config) -> str:
    """
    测试报告头部钩子
    
    在测试报告开头显示 API 基础地址信息。
    
    Args:
        config: pytest 配置对象
    
    Returns:
        str: 报告头部信息
    """
    return f"API Base URL: {BASE_URL}"


def pytest_generate_tests(metafunc: pytest.Metafunc) -> None:
    """
    自动参数化钩子
    
    根据测试函数的参数名自动添加参数化。
    - 如果测试函数有 role 参数，自动参数化为 ["PARENT", "TEACHER"]
    - 如果测试函数有 status_filter 参数，自动参数化为 ["PENDING", "PUBLISHED", "UNPUBLISHED"]
    
    Args:
        metafunc: 测试元函数对象
    """
    if "role" in metafunc.fixturenames:
        metafunc.parametrize("role", ["PARENT", "TEACHER"])
    if "status_filter" in metafunc.fixturenames:
        metafunc.parametrize("status_filter", ["PENDING", "PUBLISHED", "UNPUBLISHED"])
