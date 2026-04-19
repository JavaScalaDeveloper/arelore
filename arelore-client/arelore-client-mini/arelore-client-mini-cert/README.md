# 考证宝微信小程序（Taro + React）

该模块是“考证宝”小程序端，采用 **Taro + React** 实现，便于与 Web React 技术栈保持一致，并支持后续跨平台扩展。

## 目录结构

- `src/app.tsx`：应用入口
- `src/app.config.ts`：小程序全局配置
- `src/pages/login/index.tsx`：微信一键登录页
- `src/pages/home/index.tsx`：登录后首页
- `src/utils/request.ts`：后端请求封装
- `config/index.ts`：Taro 构建配置

## 开发与构建

```bash
npm install
npm run dev:weapp
```

开发模式会监听并输出到 `dist` 目录。  
微信开发者工具中导入该模块目录，选择 `dist` 作为小程序代码目录即可预览。

生产构建：

```bash
npm run build:weapp
```

## 登录说明

- 当前只保留 **微信一键登录**，不再使用账密登录。
- 小程序调用后端接口：`/api/user/auth/wechat/quick`。
- 登录成功后缓存：
  - `token`
  - `currentUser`

## Git 忽略说明

模块内 `.gitignore` 已忽略以下非项目核心文件：

- 构建产物：`dist/`
- 依赖目录：`node_modules/`
- 本地缓存：`.swc/`
- 旧原生小程序残留（迁移到 Taro 后不再使用）：`app.js`、`app.json`、`pages/`、`components/`、`utils/` 等

