# 小程序端应用

当前有两个独立小程序，**技术栈不同，不要混用启动方式**。

## 项目列表

| 模块 | 说明 | 技术栈 | 切环境 |
|------|------|--------|--------|
| [arelore-client-mini-word](./arelore-client-mini-word) | 背单词：词书、刷词、听词摸鱼、设置 | **微信原生**（无 Taro） | `bash start.sh [env]` → 写 `utils/env.local.js` → 开发者工具重新编译 |
| [arelore-client-mini-cert](./arelore-client-mini-cert) | 考证宝：刷题、收藏、考试记录 | Taro + React | `bash start.sh [env]` → 注入 `TARO_APP_API_BASE_URL` 并构建 `dist/` |

两端 API 约定一致：

| 环境 | user-api |
|------|----------|
| `dev` / `pre` | `http://localhost:8081/api` |
| `test` | `http://test.arelore.com:8281/api` |
| `prd` | `http://www.arelore.com:8081/api` |

Windows PowerShell 请用 Git Bash，例如：

```powershell
& "C:\DeveloperTools\Git\bin\bash.exe" start.sh dev
```

## 背单词（原生）

```bash
cd arelore-client-mini-word
bash start.sh dev          # 写入 env.local.js
# 微信开发者工具导入本目录，删掉 Storage 中 miniApiBaseUrl/miniApiEnv，再点「编译」
```

无需 `npm install`。详见该目录 README。

## 考证宝（Taro）

```bash
cd arelore-client-mini-cert
bash start.sh dev          # install + build + 后台 watcher
```

再用开发者工具导入 `arelore-client-mini-cert`（`miniprogramRoot` 指向 `dist/`）。详见该目录 README。
