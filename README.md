# 昂立在线学习平台

> 面向英语学习的微信小程序应用，支持家长/学生、教师、管理员三种角色端。

## 技术栈

| 层级 | 技术 | 版本 |
|------|------|------|
| 前端框架 | UniApp + Vue3 | Vue 3.3.x |
| 前端语言 | TypeScript | 5.x |
| 状态管理 | Pinia | 2.0.x |
| 构建工具 | Vite | 4.5.x |
| 后端框架 | Spring Boot | 3.2.x |
| 后端语言 | Java | 21 |
| ORM框架 | MyBatis Plus | 3.5.x |
| 数据库 | MySQL | 8.x+ |
| 认证 | JWT | 0.12.x |

## 项目结构

```
angli-online-learning/
├── backend/                    # 后端代码
│   ├── sql/                    # 数据库脚本
│   └── src/main/java/com/angli/online/
│       ├── controller/         # 控制器层
│       ├── service/            # 服务层
│       ├── mapper/             # 数据访问层
│       ├── entity/             # 实体类
│       ├── dto/                # 数据传输对象
│       ├── config/             # 配置类
│       └── util/               # 工具类
├── frontend/                   # 前端代码
│   └── src/
│       ├── pages/              # 页面组件
│       ├── api/                # API 接口
│       ├── stores/             # Pinia 状态管理
│       ├── utils/              # 工具函数
│       └── styles/             # 全局样式
├── ui_design/                  # UI 设计稿
└── .gitignore
```

## 功能模块

### 👨‍👩‍👧 用户端 (家长/学生)
- **首页**：今日总结、推荐课程、学习打卡
- **选课中心**：课程搜索、推荐/热门/全部 Tab、收藏功能
- **学习中心**：课程学习、课程评价(5星评分)
- **个人中心**：个人信息、绑定孩子、消息通知、设置

### 👩‍🏫 教师端
- **今日待办**：课程总结提醒
- **课程管理**：课程申请与管理
- **教师评价**：查看家长评分

### 💻 管理员端
- **Dashboard**：数据统计(用户数、营收、评分、教师数)
- **用户管理**：用户审核(教师需人工审核)
- **课程管理**：课程上架、审核、编辑、下架
- **消息公告**：发布系统公告

## 快速开始

### 环境要求
- JDK 21+
- Node.js 18+
- MySQL 8.0+

### 1. 数据库配置

```sql
-- 创建数据库
CREATE DATABASE IF NOT EXISTS angli_online DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 导入数据库脚本
source backend/sql/schema.sql;
```

### 2. 后端启动

```bash
cd backend

# 修改数据库配置 (backend/src/main/resources/application.yml)
# 配置项：spring.datasource.url, spring.datasource.username, spring.datasource.password

# 编译运行
mvn clean compile
mvn spring-boot:run
```

后端服务默认运行在 `http://localhost:8080`

### 3. 前端启动

```bash
cd frontend

# 安装依赖
npm install

# H5 开发模式
npm run dev:h5

# 微信小程序开发模式
npm run dev:mp-weixin
```

### 4. 测试账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | 123456 |
| 教师 | teacher | 123456 |
| 家长 | parent | 123456 |

## API 接口

后端提供 RESTful API，主要接口：

- `POST /api/auth/login` - 用户登录
- `POST /api/auth/register` - 用户注册
- `GET /api/courses` - 获取课程列表
- `GET /api/courses/{id}` - 获取课程详情
- `POST /api/orders` - 创建订单
- `POST /api/orders/{id}/pay` - 支付订单
- `POST /api/favorites` - 收藏课程
- `POST /api/checkin` - 每日打卡

## 构建命令

### 前端

```bash
# H5 生产构建
npm run build:h5

# 微信小程序生产构建
npm run build:mp-weixin
```

### 后端

```bash
# 打包
mvn clean package

# 运行
java -jar target/angli-online-1.0.0.jar
```

## 开发规范

- 前端使用 Vue3 Composition API + TypeScript
- 移动端使用 `rpx` 单位
- 小程序标签替换：`<div>`→`<view>`, `<span>`→`<text>`, `<img>`→`<image>`
- 样式使用 scoped + scss
- 后端采用 Controller → Service → Repository 分层架构
- 使用构造器注入方式

## License

MIT License