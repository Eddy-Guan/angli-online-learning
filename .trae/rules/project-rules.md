# angli在线项目 - 开发规则

## 项目概述

angli在线是一个面向英语学习的微信小程序应用，包含三个角色端：
- **普通用户端**：学生学习使用
- **教师端**：教师管理课程、学生
- **管理员端**：系统管理后台

**项目根目录**: `D:\AI_project\angli_online_xiaochengxu\`  
**UI设计图目录**: `D:\AI_project\angli_online_xiaochengxu\ui_design\`

---

## 技能引用规则

### 1. 后端开发 → Java Spring Boot

**技能路径**: `.trae/skills/java-springboot/`

**适用场景**:
- 创建或修改后端 API 接口
- 设计 RESTful 服务架构
- 编写 Spring Boot 业务逻辑
- 实现用户认证与授权

**核心规范**:
- 使用 Spring Boot 3.x + Java 21
- 采用 Maven 作为构建工具
- 遵循 Controller → Service → Repository 分层架构
- 使用构造器注入方式
- 使用 `@ConfigurationProperties` 进行类型安全配置

---

### 2. 数据库设计 → MySQL

**技能路径**: `.trae/skills/mysql/`

**适用场景**:
- 创建或修改 MySQL 表结构
- 设计索引策略
- 优化查询性能
- 规划数据迁移

**核心规范**:
- 使用 `BIGINT UNSIGNED AUTO_INCREMENT` 作为主键
- 使用 `utf8mb4` 字符集
- 遵循 3NF 规范化设计
- 复合索引遵循最左前缀原则
- 使用 `EXPLAIN` 分析慢查询

---

### 3. 前端开发 → UniApp Vue3 + TypeScript

**技能路径**: `.trae/skills/angli-online-uniapp/`

**适用场景**:
- 重构 UI 设计稿为 Vue3 组件
- 开发小程序页面
- 实现前后端数据交互

**核心规范**:
- 使用 Vue3 Composition API + TypeScript
- 移动端使用 `rpx` 单位，管理端使用 `px/rem/vw/vh`
- 小程序标签替换：`<div>`→`<view>`, `<span>`→`<text>`, `<img>`→`<image>`
- 样式使用 scoped + scss

---

## UI设计文件命名规范

| 前缀 | 角色端 | 目录 | 单位 |
|------|--------|------|------|
| `user_` | 普通用户端 | `pages/user/` | rpx |
| `teacher_` | 教师端 | `pages/teacher/` | rpx |
| `admin_` | 管理员端 | `pages/admin/` | px/rem/vw/vh |

**设计文件清单**:
- `user_home.html` → 用户首页
- `user_course.html` → 课程页面
- `user_learn.html` → 学习页面
- `user_profile.html` → 个人中心
- `teacher_end.html` → 教师端全部页面（需拆分）
- `admin-dashboard.html` → 管理后台仪表盘

---

## 开发工作流

1. **需求分析**: 根据用户需求确定涉及的角色端和功能模块
2. **技能加载**: 根据需求类型自动加载对应技能（后端/数据库/前端）
3. **设计参考**: 前端开发必须严格参考 `ui_design/` 下的设计稿，实现 1:1 视觉还原
4. **代码实现**: 按照各技能规范编写代码
5. **验证测试**: 确保各端功能正常且接口对接无误

---

## 技术栈总览

| 层级 | 技术 | 版本 |
|------|------|------|
| 前端框架 | UniApp + Vue3 | Vue 3.x |
| 前端语言 | TypeScript | 5.x |
| 后端框架 | Spring Boot | 3.x |
| 后端语言 | Java | 21 |
| 数据库 | MySQL | 8.x+ |
| 构建工具 | Maven / npm | - |