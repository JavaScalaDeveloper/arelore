# Arelore Client Web

基于 React + JSX + Ant Design 的企业级 Web 应用。

## 技术栈

- **React**: 18.2.0
- **React Router**: 6.20.0 (路由管理)
- **Ant Design**: 5.12.0 (UI 组件库)
- **Node.js**: v24.11.1

## 功能特性

### 首页功能
✅ **顶部导航栏**
- 品牌 Logo
- 分类导航（首页、产品中心、解决方案、联系我们）
- 响应式设计

✅ **用户系统**
- 登录/注册入口
- 用户中心（个人中心、账号设置）
- 用户头像下拉菜单
- 退出登录功能

✅ **消息通知**
- 未读消息提醒（角标显示）
- 通知中心入口

✅ **页面布局**
- Hero 展示区
- 特性卡片展示
- 页脚信息（友情链接、版权信息）

## 目录结构

```
arelore-client-web/
├── public/                 # 静态资源
│   └── index.html         # HTML 模板
├── src/                    # 源代码
│   ├── components/        # 公共组件（待扩展）
│   ├── pages/             # 页面组件（待扩展）
│   ├── utils/             # 工具函数（待扩展）
│   ├── App.js             # 主应用组件
│   ├── App.css            # 应用样式
│   ├── index.js           # 应用入口
│   └── index.css          # 全局样式
├── package.json           # 项目配置
└── README.md              # 说明文档
```

## 快速开始

### 安装依赖

```bash
cd arelore-client-web
npm install
```

### 启动开发服务器

```bash
npm start
```

访问 http://localhost:3000 查看应用

### 构建生产版本

```bash
npm run build
```

### 运行测试

```bash
npm test
```

## 页面预览

### 首页布局
- **顶部导航栏**: 深色主题，包含 Logo、导航菜单、用户功能区
- **Hero 区域**: 渐变色背景，大标题和行动按钮
- **特性展示**: 三列卡片布局，悬停动效
- **页脚**: 深色主题，包含版权和友情链接

### 用户交互
- 导航菜单高亮当前页面
- 用户头像下拉菜单（个人中心、账号设置、退出登录）
- 消息通知角标提醒
- 登录/注册按钮切换

## 下一步开发

建议继续开发以下页面：
- [ ] 登录页面 (`/login`)
- [ ] 注册页面 (`/register`)
- [ ] 个人中心页面 (`/user/profile`)
- [ ] 账号设置页面 (`/user/settings`)
- [ ] 产品中心页面 (`/products`)
- [ ] 解决方案页面 (`/solutions`)
- [ ] 联系我们页面 (`/contact`)

## 开发规范

- 使用 JSX 语法编写组件
- 使用 Ant Design 5.x 组件库
- 遵循 React Hooks 最佳实践
- 组件化、模块化开发
- 使用 React Router 6.x 进行路由管理
- 响应式设计，适配移动端
