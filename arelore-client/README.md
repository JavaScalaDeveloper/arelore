# Arelore Client

前端客户端模块，包含多个平台的客户端应用。

## 模块结构

```
arelore-client/
├── arelore-client-web/              # Web 端应用 (React + Ant Design)
├── arelore-client-mini-program/     # 小程序端应用 (微信/支付宝)
├── arelore-client-android/          # Android 原生应用
├── arelore-client-ios/              # iOS 原生应用
└── README.md                        # 本说明文档
```

## 各平台说明

### 1. Web 端 (arelore-client-web)
- **技术栈**: React 18 + Ant Design 5 + TypeScript
- **开发环境**: Node.js v24+
- **构建工具**: Vite / Create React App
- **路由管理**: React Router 6
- **状态管理**: Redux / Zustand (可选)
- **功能特性**:
  - 响应式设计，支持桌面和移动端
  - 用户认证系统
  - 实时消息通知
  - 数据可视化展示

**快速开始**:
```bash
cd arelore-client-web
npm install
npm start
```

### 2. 小程序 (arelore-client-mini-program)
- **平台支持**: 微信小程序、支付宝小程序
- **开发框架**: Taro / uni-app (推荐)
- **语言**: JavaScript / TypeScript
- **功能特性**:
  - 跨平台开发，一套代码多端运行
  - 原生体验
  - 即用即走

**快速开始**:
```bash
cd arelore-client-mini-program
npm install
npm run dev:weapp  # 微信小程序
npm run dev:alipay # 支付宝小程序
```

### 3. Android (arelore-client-android)
- **开发语言**: Kotlin / Java
- **最低版本**: Android 5.0 (API 21)
- **开发工具**: Android Studio
- **架构模式**: MVVM
- **功能特性**:
  - Material Design 设计
  - 离线缓存
  - 推送通知
  - 相机、相册等原生功能调用

**快速开始**:
```bash
cd arelore-client-android
# 使用 Android Studio 打开项目
# 配置 Gradle 依赖
# 运行到模拟器或真机
```

### 4. iOS (arelore-client-ios)
- **开发语言**: Swift / Objective-C
- **最低版本**: iOS 13.0+
- **开发工具**: Xcode
- **架构模式**: MVVM / VIPER
- **功能特性**:
  - 遵循 iOS 设计规范
  - 离线缓存
  - 推送通知
  - 相机、相册等原生功能调用

**快速开始**:
```bash
cd arelore-client-ios
# 使用 Xcode 打开项目
# 配置 CocoaPods 依赖
# 运行到模拟器或真机
```

## 技术栈总览

| 平台 | 主要技术 | UI 框架 | 状态管理 |
|------|---------|--------|----------|
| Web | React + TypeScript | Ant Design | Redux/Zustand |
| 小程序 | Taro/uni-app | Vant Weapp | Vuex/Pinia |
| Android | Kotlin | Material Components | ViewModel |
| iOS | Swift | SwiftUI/UIKit | Combine |

## API 接口

所有客户端都通过 RESTful API 与后端服务通信：
- **基础 URL**: `http://localhost:8080/api` (开发环境)
- **认证方式**: JWT Token
- **数据格式**: JSON

## 开发规范

1. **代码风格**: 
   - Web 端遵循 ESLint + Prettier 规范
   - 移动端遵循各自平台的代码规范
   
2. **Git 提交**: 使用 Conventional Commits 规范
   ```
   feat: 新功能
   fix: 修复 bug
   docs: 文档更新
   style: 代码格式调整
   refactor: 重构代码
   test: 测试相关
   chore: 构建/工具链相关
   ```

3. **分支管理**:
   - `main`: 主分支，生产环境代码
   - `develop`: 开发分支
   - `feature/*`: 功能分支
   - `bugfix/*`: 修复分支

## 构建部署

### Web 端
```bash
npm run build
# 生成文件在 dist/ 目录
```

### 小程序
```bash
npm run build:weapp
# 生成文件在 dist/weapp/ 目录
```

### Android
```bash
./gradlew assembleRelease
# 生成 APK 在 app/build/outputs/apk/release/
```

### iOS
```bash
# 在 Xcode 中选择 Product -> Archive
# 导出 IPA 文件
```

## 常用命令

### Web 端
```bash
npm start          # 启动开发服务器
npm run build      # 构建生产包
npm run lint       # 代码检查
npm test           # 运行测试
```

### 小程序
```bash
npm run dev:weapp  # 开发微信小程序
npm run build:weapp # 构建微信小程序
```

## 下一步计划

- [ ] 完善 Web 端功能
- [ ] 启动小程序开发
- [ ] 启动 Android 原生开发
- [ ] 启动 iOS 原生开发
- [ ] 统一多端设计规范
- [ ] 实现跨端状态同步

## 常见问题

**Q: 为什么选择多端开发？**
A: 不同用户有不同的使用场景，多端覆盖可以提供最佳的用户体验。

**Q: 如何保证多端体验一致？**
A: 通过统一的设计规范和组件库，确保视觉和交互的一致性。

**Q: API 版本如何管理？**
A: 在 URL 中包含版本号，如 `/api/v1/products`，便于后续迭代。

## 联系方式

如有问题，请联系开发团队或提交 Issue。
