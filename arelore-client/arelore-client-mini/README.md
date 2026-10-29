# 小程序端应用

当前有两个独立小程序，**技术栈不同，不要混用启动方式**。

## 项目列表

| 模块 | 说明 | 技术栈 | 切环境 |
|------|------|--------|--------|
| [arelore-client-mini-word](./arelore-client-mini-word) | 背单词：词书、刷词、听词摸鱼、设置 | **微信原生**（无 Taro） | `bash start.sh [env]` → 写 `utils/env.local.js` → 开发者工具重新编译 |
| [arelore-client-mini-cert](./arelore-client-mini-cert) | 考证宝：刷题、收藏、考试记录 | Taro + React | `bash start.sh [env]` → 注入 `TARO_APP_API_BASE_URL` 并构建 `dist/` |

---

## 后端域名与环境（重点）

两端 **API 约定一致**。本地用 `localhost`，测试机用 `test.arelore.com`，**正式发布用 `www.arelore.com`（HTTPS）**。

| 环境参数 | 用途 | user-api Base URL | 说明 |
|----------|------|-------------------|------|
| `dev` | 本机联调 | `http://localhost:8081/api` | 对齐 `application-dev.yml` / `server.port=8081` |
| `pre` | 本机预发（配置同 test、端口同 prd） | `http://localhost:8081/api` | 一般仍打本机 |
| `test` | 测试机 | `http://test.arelore.com:8281/api` | 对齐 `application-test.yml` → `8281`；**仅开发者工具**（可关 URL 校验） |
| `prd` | **正式版 / 提审 / 线上** | `https://www.arelore.com/api` | 微信 **request 合法域名** 须 **HTTPS**，且 **不能带端口号** |

> 注意：以前写过的 `http://www.arelore.com:8081/api` **不能**用于小程序正式版。  
> 线上应由 Nginx（或同类反向代理）把 `https://www.arelore.com/api` 转到本机 `8081` 的 user 服务。

Windows PowerShell 请用 Git Bash，例如：

```powershell
& "C:\DeveloperTools\Git\bin\bash.exe" start.sh prd
# 或本机：
& "C:\DeveloperTools\Git\bin\bash.exe" start.sh dev
```

---

## 不同环境如何请求不同后端（原理）

小程序**没有**像 Web 那样在运行时读 `.env` 的通用机制，因此我们用「**编译前写入配置 → 再编译/构建**」把域名打进包里。

### 总览

```text
本地 / 测试 / 正式
       │
       ▼
  bash start.sh <env>          ← 唯一推荐入口
       │
       ├─ mini-word（原生）
       │     写入 utils/env.local.js（gitignore）
       │     → 微信开发者工具「编译」时 require 该文件
       │
       └─ mini-cert（Taro）
             export TARO_APP_API_BASE_URL=...
             → webpack DefinePlugin 打进 dist/
             → 开发者工具打开 dist/ 编译上传
```

### 解析优先级

两边都支持「临时覆盖」，避免改代码：

| 优先级（高→低） | 背单词（原生） | 考证宝（Taro） |
|-----------------|----------------|----------------|
| 1 | Storage `miniApiBaseUrl`（完整 URL） | Storage `miniApiBaseUrl` |
| 2 | Storage `miniApiEnv`（`dev`/`test`/`pre`/`prd`） | — |
| 3 | `utils/env.local.js`（`start.sh` 生成） | 构建时 `TARO_APP_API_BASE_URL` |
| 4 | 代码内默认 `test` | 代码内默认 `http://localhost:8081/api` |

**提审 / 正式发布前务必：**

1. 执行 `bash start.sh prd`
2. 在开发者工具 **清除 Storage** 中的 `miniApiBaseUrl`、`miniApiEnv`（否则会盖掉正式域名）
3. 重新编译 → 上传 → 提交审核  
4. 微信公众平台 → 开发管理 → 开发设置 → **服务器域名**：把  
   `https://www.arelore.com`  
   配进 **request 合法域名**（不要写端口、不要写 http）

---

## 本机开发（localhost）

1. 启动 user 后端（`8081`），例如：

   ```bash
   cd arelore-server/arelore-server-user
   bash start.sh dev
   ```

2. 切小程序到本地 API：

   ```bash
   # 背单词
   cd arelore-client-mini/arelore-client-mini-word
   bash start.sh dev

   # 考证宝
   cd arelore-client-mini/arelore-client-mini-cert
   bash start.sh dev
   ```

3. 微信开发者工具：

   - 背单词：导入 `arelore-client-mini-word` 目录，点「编译」
   - 考证宝：导入 `arelore-client-mini-cert`（`miniprogramRoot` → `dist/`）
   - 详情 → 本地设置 → **不校验合法域名**（localhost / 带端口的 test 域名都需要）

4. 真机预览本机服务时，不要用 `localhost`（手机访问的是手机自己），改用电脑局域网 IP，例如：

   ```bash
   MINI_WORD_API_BASE_URL=http://192.168.1.10:8081/api bash start.sh dev
   # 考证宝：
   MINI_CERT_API_BASE_URL=http://192.168.1.10:8081/api bash start.sh dev
   ```

---

## 测试机

```bash
bash start.sh test
# → http://test.arelore.com:8281/api
```

开发者工具里同样建议「不校验合法域名」。线上体验版若要走测试机，需另配可 HTTPS 的测试域名（微信正式校验不支持 `:8281`）。

---

## 正式发布（www.arelore.com）

```bash
# 背单词
cd arelore-client-mini/arelore-client-mini-word
bash start.sh prd
# 打开开发者工具 → 清掉 miniApiBaseUrl / miniApiEnv → 编译 → 上传

# 考证宝
cd arelore-client-mini/arelore-client-mini-cert
bash start.sh prd
# 打开开发者工具（dist）→ 清 Storage 覆盖 → 上传
```

成功后背单词的 `utils/env.local.js` 应类似：

```js
module.exports = {
  env: 'prd',
  apiBaseUrl: 'https://www.arelore.com/api'
};
```

`env.local.js` **已 gitignore**，不会提交；每人/每台机器用 `start.sh` 生成即可。

### 运维侧检查清单

- [ ] `https://www.arelore.com/api/...` 能反代到 user 服务（如 `127.0.0.1:8081`）
- [ ] TLS 证书有效
- [ ] 微信后台已配置 request 合法域名：`https://www.arelore.com`
- [ ] 上传的体验版/正式版包是用 `start.sh prd` 打出来的（不是残留的 `dev` / `test`）

---

## 背单词（原生）快速命令

```bash
cd arelore-client-mini-word
bash start.sh dev          # 本机 localhost:8081
bash start.sh test         # 测试机
bash start.sh prd          # 正式 https://www.arelore.com/api
# 微信开发者工具导入本目录，删掉 Storage 覆盖项，再点「编译」
```

无需 `npm install`。细节见 [arelore-client-mini-word/README.md](./arelore-client-mini-word/README.md)。

---

## 考证宝（Taro）快速命令

```bash
cd arelore-client-mini-cert
bash start.sh dev          # install + build + 后台 watcher
bash start.sh prd          # 正式域名构建
```

再用开发者工具导入 `arelore-client-mini-cert`（`miniprogramRoot` 指向 `dist/`）。详见 [arelore-client-mini-cert/README.md](./arelore-client-mini-cert/README.md)。

---

## 排障

| 现象 | 常见原因 |
|------|----------|
| 一直打到 localhost / test 机 | 忘了 `start.sh prd`；或 Storage 里残留 `miniApiBaseUrl` |
| 正式版报「不在以下 request 合法域名中」 | 微信后台未配 `https://www.arelore.com`；或包内仍是 `http://` / 带端口 |
| 本机模拟器不通 | user 未起；或 `env.local.js` 不是 `dev`；或未重新编译 |
| 真机调试本机不通 | 用了 `localhost`，应改为电脑局域网 IP |
| 考证宝改了 start.sh 但地址不变 | 需重新跑 `start.sh` 触发构建；确认打开的是最新 `dist/` |
