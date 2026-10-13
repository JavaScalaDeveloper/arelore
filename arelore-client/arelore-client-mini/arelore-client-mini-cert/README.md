# 考证宝微信小程序（Taro + React）

该模块是“考证宝”小程序端，采用 **Taro + React** 实现，便于与 Web React 技术栈保持一致，并支持后续跨平台扩展。

## 目录结构

- `src/app.tsx`：应用入口
- `src/app.config.ts`：小程序全局配置
- `src/pages/login/index.tsx`：微信一键登录页
- `src/pages/home/index.tsx`：登录后首页
- `src/utils/request.ts`：后端请求封装
- `config/index.ts`：Taro 构建配置
- `start.sh`：按环境注入 `TARO_APP_API_BASE_URL` 并启动 `dev:weapp`

## 切换接口环境

与 user 服务约定（与背单词小程序一致）：

| 环境 | API |
|------|-----|
| `dev` / `pre` | `http://localhost:8081/api` |
| `test` | `http://www.arelore.com:8281/api` |
| `prd` | `http://www.arelore.com:8081/api` |

推荐用 `start.sh`（会 `npm install`、构建，并后台跑 `dev:weapp`）：

```bash
cd arelore-client/arelore-client-mini/arelore-client-mini-cert
bash start.sh dev    # 本地
bash start.sh test   # 测试机
```

Windows PowerShell 请用 **Git 自带的 bash**（系统 `sh` / `bash` 常指向 WSL 空壳）：

```powershell
cd arelore-client\arelore-client-mini\arelore-client-mini-cert
& "C:\DeveloperTools\Git\bin\bash.exe" start.sh dev
# 或：& "C:\Program Files\Git\bin\bash.exe" start.sh test
```

脚本通过环境变量 `TARO_APP_API_BASE_URL` 注入构建；也可临时覆盖：

```bash
MINI_CERT_API_BASE_URL=http://192.168.1.10:8081/api bash start.sh dev
```

**若小程序仍请求旧地址**：在微信开发者工具 Storage 中删掉 `miniApiBaseUrl`（运行时覆盖会高于构建注入）。

解析优先级：Storage `miniApiBaseUrl` → 构建时 `TARO_APP_API_BASE_URL` → 默认 `http://localhost:8081/api`。

## 开发与构建

不走 `start.sh` 时也可手动：

```bash
npm install
# 可选：export TARO_APP_API_BASE_URL=http://localhost:8081/api
npm run dev:weapp
```

开发模式会监听并输出到 `dist` 目录。

**微信开发者工具导入方式（二选一）：**

1. **推荐**：导入本目录 `arelore-client-mini-cert`（已含根目录 `project.config.json`，`miniprogramRoot` 指向 `dist/`）。
2. 或直接导入 `dist/` 目录。

导入前请先执行 `bash start.sh [env]`、`npm run build:weapp` 或 `npm run dev:weapp`，确保 `dist/app.json` 已生成。若报 `no found app.json` 或 `prebundle/...react-dom... is not defined`，多为未构建或打开了错误目录。

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
