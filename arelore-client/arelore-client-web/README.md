# Arelore Client Web

基于 React + TypeScript + Ant Design 的企业级 Web 应用。

## 技术栈

- **React**: 18.2.0
- **TypeScript**: 4.9.x（随 react-scripts）
- **React Router**: 6.20.0 (路由管理)
- **Ant Design**: 5.12.0 (UI 组件库)
- **Axios**: 1.6.2 (HTTP 客户端)
- **pdfjs-dist** + **docx**：文档处理「PDF 转 Word」（纯前端，见下文）
- **Node.js**：建议使用 LTS（如 18+ / 20+），用于 `npm install` / 构建

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
│   ├── api/               # API 接口（TypeScript）
│   │   ├── auth.tsx      # 认证相关 API
│   │   └── user.tsx      # 用户相关 API
│   ├── pages/             # 页面组件（TypeScript）
│   │   ├── HomePage.tsx   # 首页
│   │   └── LoginPage.tsx  # 登录页
│   ├── types/             # TypeScript 类型定义
│   │   └── index.ts       # 通用类型
│   ├── utils/             # 工具函数（TypeScript）
│   │   └── request.tsx    # Axios 封装
│   ├── App.tsx            # 主应用组件（TypeScript）
│   ├── App.css            # 应用样式
│   ├── index.tsx          # 应用入口（TypeScript）
│   └── index.css          # 全局样式
├── package.json           # 项目配置
├── tsconfig.json          # TypeScript 配置
└── README.md              # 说明文档
```

## 快速开始

### 安装依赖

```bash
cd arelore-client-web
npm install
```

`npm install` 结束后会执行 **`postinstall`**：将 `pdfjs-dist` 自带的 `pdf.worker.min.js` 复制到 `public/pdf.worker.min.js`，供浏览器解析 PDF 使用。**无需安装浏览器插件**；也无需单独下载 Adobe 等客户端，仅需上述 Node 依赖。

若复制失败（例如尚未安装依赖就删除了 `node_modules`），可手动执行：

```bash
node scripts/copy-pdf-worker.js
```

### 文档处理：PDF 转 Word（免费工具）

- **入口**：顶部菜单 **免费工具 → 文档处理 → PDF转Word**，路由 `/home/tools/pdf-to-word`。
- **行为**：在浏览器本地读取 PDF 文本并生成 `.docx` 下载，**文件不上传服务器**。
- **限制**：
  - 依赖 PDF **文字层**；纯扫描件、图片型 PDF 可能几乎没有可导出文字。
  - 导出结果为**纯文本编排**，不保证与原 PDF 版式、图片、表格一致。
- **部署在子路径时**：需正确设置 `package.json` 的 `homepage` 或构建时的 `PUBLIC_URL`，否则 `pdf.worker.min.js` 可能 404（Worker 路径与 `PUBLIC_URL` 一致）。

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

- 使用 TypeScript + React 语法编写组件
- 使用 Ant Design 5.x 组件库
- 遵循 React Hooks 最佳实践
- 组件化、模块化开发
- 使用 React Router 6.x 进行路由管理
- 响应式设计，适配移动端
- 所有代码必须使用 TypeScript 类型注解
