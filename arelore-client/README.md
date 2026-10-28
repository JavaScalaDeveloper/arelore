# Arelore Client

前端客户端模块，包含多个平台的客户端应用。

## 模块结构

```
arelore-client/
├── arelore-client-web/                 # 用户端 Web (React + Ant Design)
├── arelore-client-web-admin/           # 管理端 Web
├── arelore-client-mini/
│   ├── arelore-client-mini-word/       # 背单词小程序（微信原生）
│   └── arelore-client-mini-cert/       # 考证宝小程序（Taro + React）
├── arelore-client-android/             # Android 原生应用
├── arelore-client-ios/                 # iOS 原生应用
└── README.md
```

## API 环境约定

与各模块 `start.sh` 对齐：

| 环境 | user-api | admin-api |
|------|----------|-----------|
| dev / pre | `http://localhost:8081/api` | `http://localhost:8082/api` |
| test | `http://test.arelore.com:8281/api` | `http://test.arelore.com:8282/api` |
| prd | `http://www.arelore.com:8081/api` | `http://www.arelore.com:8082/api` |

- **认证方式**: JWT Token
- **数据格式**: JSON

Windows PowerShell 执行各模块 `start.sh` 时请用 **Git Bash**（系统 `sh`/`bash` 常指向 WSL 空壳）：

```powershell
& "C:\DeveloperTools\Git\bin\bash.exe" start.sh dev
# 或：& "C:\Program Files\Git\bin\bash.exe" start.sh test
```

## 各平台说明

### 1. 用户端 Web (`arelore-client-web`)

```bash
cd arelore-client-web
bash start.sh dev    # 端口 3300，API → localhost:8081
# 或 npm start（默认读 .env）
```

详见 [arelore-client-web/README.md](./arelore-client-web/README.md)。

### 2. 管理端 Web (`arelore-client-web-admin`)

```bash
cd arelore-client-web-admin
bash start.sh dev    # 端口 3301，API → localhost:8082
```

详见 [arelore-client-web-admin/README.md](./arelore-client-web-admin/README.md)。

### 3. 小程序 (`arelore-client-mini`)

两个独立小程序，**启动方式不同**：

| 模块 | 技术栈 | 本地启动 |
|------|--------|----------|
| [mini-word](./arelore-client-mini/arelore-client-mini-word) | 微信原生 | `bash start.sh dev` 写 `env.local.js` → 开发者工具导入本目录并编译 |
| [mini-cert](./arelore-client-mini/arelore-client-mini-cert) | Taro + React | `bash start.sh dev` 构建并 watcher → 开发者工具导入本目录（`dist/`） |

总览见 [arelore-client-mini/README.md](./arelore-client-mini/README.md)。

### 4. Android / iOS

见各自目录 README。当前以 Web 与小程序为主。

## 开发规范

1. **代码风格**: Web 端遵循 ESLint + Prettier；移动端遵循各平台约定
2. **Git 提交**: Conventional Commits（`feat` / `fix` / `docs` 等）
3. **分支**: `main` / `develop` / `feature/*` / `bugfix/*`

## 常见问题

**Q: PowerShell 里 `sh start.sh` / `bash start.sh` 没效果？**  
A: 用 Git 安装目录下的 `bash.exe` 调用（见上文），不要用系统自带的 WSL 空壳。

**Q: 小程序仍请求旧 API 地址？**  
A: 在微信开发者工具 Storage 中删除 `miniApiBaseUrl`（及 word 的 `miniApiEnv`），再重新编译 / 重新跑 `start.sh`。
