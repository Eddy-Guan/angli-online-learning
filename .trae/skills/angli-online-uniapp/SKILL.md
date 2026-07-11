---
name: angli-wechat-mini-program
description: Use this skill when implementing 昂立教育 WeChat Mini Program pages using Vue3 + TypeScript + uni-app based on design docs. Triggers: "今日总结", "daily-summary", UI design requirement, or design frame paths. Focus strictly on layout structure, spacing, colors, rounding, and interactions.
---
Skill Body:
昂立教育小程序 — 今日总结页面实现指南（Vue3 + uni-app）
核心要求
严格按照设计需求文档输出布局、间距、颜色、圆角等视觉规范。课程名称、教师姓名等文字统一使用黑色类字体（#1d1d1f 主文字）。
1. 页面整体布局（从上到下）

顶部导航栏：左侧返回箭头图标、中间标题「今日总结」（18px 加粗）、右侧搜索图标
总结列表区：上下内边距 12px，左右内边距 16px，课程组之间垂直间距 12px
底部导航栏：固定底部，白色背景 + 1px 上边框，四个 Tab（首页、课程、学习、我的），当前页高亮橙色 #F05A22

2. 课程消息组（每个课程一个）
标题栏（可点击折叠/展开）：

左：40×40px 圆角 10px 科目图标
中上：课程标题文字（14px，加粗）
中下：教师姓名 + 更新数量（11px，灰色）
右：未读数角标（橙色圆角药丸，10px，最小宽度 18px）+ 折叠箭头图标（展开向下，折叠时旋转 -90deg 朝右）

消息列表（折叠区域）：

内边距 10px 14px 14px
每条消息间距 8px

单条消息：

左：36×36px 圆形教师头像
中上：教师姓名（12px 加粗）+ 时间（10px 灰色），左右分布
中下：消息内容预览（12px 灰色，最多显示 2 行）+ 圆角色标签（4px 圆角）
右（可选）：未读红点（8×8px 橙色）
整条消息可点击

3. 全屏详情页

纯白背景 #FFFFFF
顶部：左侧返回✕ + 中间标题
正文：时间信息（11px 灰色）+ 消息全文（14px，行高 1.7）

4. 颜色系统（严格使用）

品牌主色：#F05A22
品牌浅色：#fde8e1
品牌渐变：#ff7a45
页面背景：#F5F5F7
卡片背景：#FFFFFF
主文字：#1d1d1f（课程、姓名等统一使用）
次要文字：#6e6e73
浅色文字：#999
边框：#e8e8ed

5. 圆角系统

卡片/消息组：12px
角标/标签：9px / 4px
头像：50%

6. 其他

图标：全部使用内联 SVG（返回箭头、搜索、折叠箭头、底部导航图标等）
空状态：居中灰色图标 + 文案「暂无今日总结，老师会在课后推送学习总结」
交互：点击标题栏折叠展开（箭头动画），点击消息打开全屏详情页

实现原则：以设计文档为准，只需确保布局结构、间距、颜色、圆角完全一致即可。文字颜色统一使用主黑色系。