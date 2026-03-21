# Arelore Client 项目结构

本项目包含多个客户端模块，用于不同平台的应用开发。

## 目录结构

```
arelore-client/
├── arelore-client-web/         # Web 端应用 (React + Ant Design) ✅已完工
├── arelore-client-mini-program/  # 小程序端应用 🔄待初始化
├── arelore-client-android/       # Android 原生应用 🔄待初始化
├── arelore-client-ios/           # iOS 原生应用 🔄待初始化
└── README.md                     # 项目说明文档
```

## 各模块说明

### 1. arelore-client-web ✅
- **技术栈**: 
  - React 18.2.0
  - React Router 6.20.0
  - Ant Design 5.12.0
- **Node 版本**: v24.11.1
- **用途**: Web 浏览器端应用
- **功能特性**:
  - ✅ 顶部导航栏（品牌 Logo、分类导航）
  - ✅ 用户系统（登录/注册、用户中心、退出登录）
  - ✅ 消息通知（未读消息提醒）
  - ✅ Hero 展示区
  - ✅ 特性卡片展示
  - ✅ 响应式设计（适配 PC 和移动端）
  - ✅ 页脚信息（友情链接、版权信息）

### 2. arelore-client-mini-program 🔄
- **用途**: 微信小程序、支付宝小程序等小程序端应用
- **状态**: 待初始化
- **规划技术栈**: Taro / uni-app

### 3. arelore-client-android 🔄
- **用途**: Android 原生移动应用
- **状态**: 待初始化
- **规划技术栈**: Kotlin / Java + Android Studio

### 4. arelore-client-ios 🔄
- **用途**: iOS 原生移动应用
- **状态**: 待初始化
- **规划技术栈**: Swift / Objective-C + Xcode

## 快速开始

### Web 端开发

1. 进入 arelore-client-web 目录：
```bash
cd arelore-client-web
```

2. 安装依赖：
```bash
npm install
```

3. 启动开发服务器：
```bash
npm start
```

访问 http://localhost:3000 查看应用

4. 构建生产版本：
```bash
npm run build
```

其他模块的详细说明请参考各自目录下的 README 文档。

## 技术选型理由

### Web 端
- **React 18**: 最新的稳定版本，性能优异，生态完善
- **Ant Design 5**: 企业级 UI 组件库，符合大厂设计规范
- **React Router 6**: 最新路由方案，更好的类型安全和代码分割
- **Node.js 24**: 使用最新 LTS 版本，获得最佳性能和安全性

## 项目特色

1. **大厂风范**: 参考业内互联网大厂官网设计
2. **用户体验**: 流畅的交互动画，响应式布局
3. **模块化**: 清晰的目录结构，便于维护和扩展
4. **现代化**: 使用最新技术栈，面向未来开发
