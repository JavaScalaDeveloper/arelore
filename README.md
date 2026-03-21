# Arelore 项目结构

本项目是一个全栈应用，包含前端客户端和后端服务。

## 目录结构

```
arelore/
├── arelore-client/          # 前端客户端应用
│   ├── arelore-client-web/         # Web 端应用 (React + Ant Design) ✅
│   ├── arelore-client-mini-program/  # 小程序端应用 🔄
│   ├── arelore-client-android/       # Android 原生应用 🔄
│   ├── arelore-client-ios/           # iOS 原生应用 🔄
│   └── README.md                     # 前端说明文档
├── arelore-server/          # 后端服务应用
│   └── README.md            # 后端说明文档
└── README.md                # 项目总览
```

## 模块说明

### 1. arelore-client (前端客户端)
包含多个平台的客户端应用：
- **Web 端**: React + Ant Design ✅
  - 顶部导航栏、用户系统、消息通知
  - Hero 展示区、响应式设计
- **小程序**: 微信/支付宝小程序 🔄
- **Android**: 原生移动应用 🔄
- **iOS**: 原生移动应用 🔄

### 2. arelore-server (后端服务)
- **技术栈**: Java 21 + Maven 3.8
- **框架**: Spring Boot 3.2.x
- **功能**: RESTful API、业务逻辑、数据存储
- **数据库**: MySQL / PostgreSQL
- **缓存**: Redis
- **认证**: JWT

## 快速开始

### 前端开发
```bash
cd arelore/arelore-client/arelore-client-web
npm install
npm start
```

访问 http://localhost:3000

### 后端开发
```bash
cd arelore/arelore-server
mvn clean install
mvn spring-boot:run
```

访问 http://localhost:8080

详细文档请参考各模块下的 README。

## 技术栈总览

### 前端
- React 18.2.0
- Ant Design 5.12.0
- React Router 6.20.0
- Node.js v24.11.1

### 后端
- Java 21
- Maven 3.8
- Spring Boot 3.2.x
- MySQL/PostgreSQL
- Redis
- JWT

## 项目特色

1. **前后端分离**: 清晰的架构划分，便于维护和扩展
2. **多端支持**: Web、小程序、Android、iOS 全覆盖
3. **大厂风范**: 参考业内互联网大厂设计规范
4. **现代化**: 使用最新技术栈，面向未来开发
5. **用户体验**: 流畅的交互动画，响应式布局
