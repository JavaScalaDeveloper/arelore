# 背单词微信小程序（原生）

模块路径：`arelore-client/arelore-client-mini/arelore-client-mini-word`

用 **微信原生小程序**（WXML / WXSS / JS）实现，**不使用 Taro**。已接入词书切换、学习计划、新学/复习刷词、听词摸鱼、偏好设置与微信一键登录。

| 项 | 值 |
|------|------|
| 名称 | 背单词 |
| 技术栈 | 微信原生小程序，无 npm / 无 Taro |
| 最低基础库 | 建议 2.10.4+（`getUserProfile`） |
| 登录 | 微信一键登录（`getUserProfile` + `wx.login` + `/api/user/auth/wechat/quick`） |

---

## 已实现功能

1. **底部两个菜单**：`单词`、`我的`（原生 `tabBar`）。
2. **单词页**：当前词书、整书进度、新学/复习入口；网络不通时显示错误态与接口地址。
3. **选词本**：语种 → 分类 → 词书 → 学习计划（每日新学、复习比例、发音）。
4. **学习页**：新学/复习队列；看词选义 / 听词摸鱼（字母拼读 + 中文 TTS）。
5. **我的 / 设置**：头像昵称、学习天数、背词模式与拼读速度；退出登录。
6. **微信一键登录**。

---

## 页面结构

```
arelore-client-mini-word/
├── app.js / app.json / app.wxss
├── project.config.json
├── start.sh                # 写入 utils/env.local.js（切环境）
├── assets/                 # tabBar 图标
├── utils/
│   ├── config.js           # 接口根地址（Storage / env.local / 默认）
│   ├── env.local.js.example
│   ├── request.js
│   ├── storage.js
│   ├── preference.js
│   └── tts.js
└── pages/
    ├── words/              # 单词（tab）
    ├── profile/            # 我的（tab）
    ├── books/              # 选词本 + 计划
    ├── study/              # 刷词
    ├── settings/           # 背词设置
    └── login/              # 微信一键登录
```

---

## 怎样打开项目

本模块 **没有** `npm install` / `npm run dev`。用微信开发者工具直接导入源码即可。

1. 安装 [微信开发者工具](https://developers.weixin.qq.com/miniprogram/dev/devtools/download.html)
2. 选择 **导入项目**
3. 目录选：  
   `arelore-client/arelore-client-mini/arelore-client-mini-word`
4. AppID：
   - 仅看界面：选 **测试号 / 游客模式**（工程里默认 `touristappid`）
   - 真机预览、头像昵称组件：换成你自己的小程序 AppID
5. 不勾选「使用 npm」；编译模式选普通小程序

导入后应看到底部 **单词 / 我的**。若提示找不到 `app.json`，说明打开的不是本目录（不要打开 `arelore-client-mini-cert` 的 `dist/`）。

---

## 切换接口环境（本地 / 测试 / 生产）

小程序**不会**像考证宝那样由 `start.sh` 起 watcher；脚本只**写入** `utils/env.local.js`，再在微信开发者工具里**重新编译**后生效。

### 环境表

| 环境 | API |
|------|-----|
| `dev` / `pre` | `http://localhost:8081/api` |
| `test`（未配置时的默认） | `http://www.arelore.com:8281/api` |
| `prd` | `http://www.arelore.com:8081/api` |

### 推荐：用 start.sh 切到本地

```bash
cd arelore-client/arelore-client-mini/arelore-client-mini-word
bash start.sh dev
```

Windows PowerShell 请用 **Git 自带的 bash**（系统 `sh` / `bash` 常指向 WSL 空壳，会失败或行为异常）：

```powershell
cd arelore-client\arelore-client-mini\arelore-client-mini-word
& "C:\DeveloperTools\Git\bin\bash.exe" start.sh dev
# 若 Git 装在默认路径：
# & "C:\Program Files\Git\bin\bash.exe" start.sh dev
```

成功时应看到 **`已写入 utils/env.local.js`**，内容类似：

```js
module.exports = {
  env: 'dev',
  apiBaseUrl: 'http://localhost:8081/api'
};
```

然后：

1. 打开微信开发者工具，导入本目录（含 `project.config.json`）
2. **删掉** Storage 里的 `miniApiBaseUrl`、`miniApiEnv`（有的话会覆盖 `env.local.js`）
3. 点击「编译」
4. 确认本机 user 服务已在 `8081` 启动；`project.config.json` 保持 `urlCheck: false`

也可不跑脚本，直接复制示例文件再改：

```powershell
copy utils\env.local.js.example utils\env.local.js
```

### 解析优先级（高 → 低）

1. Storage `miniApiBaseUrl`（完整地址，临时覆盖）
2. Storage `miniApiEnv`（`dev` / `test` / `pre` / `prd`）
3. `utils/env.local.js`（由 `bash start.sh [env]` 生成，已 gitignore）
4. 默认 `test`

临时在开发者工具 Storage 写入 `miniApiEnv = dev` 也可切本地；若曾设过 `miniApiBaseUrl`，改环境前请先删掉该项。

### 网络不通时

单词页、选词本、学习、登录、我的页会提示「网络不通」，并尽量展示当前环境与接口地址，可点「重试」。

---

## 页面说明

### 单词

- 顶栏：当前单词本名称 +「切换」
- 整书进度、新学 / 复习卡片（可进入刷词）
- 听词摸鱼模式下展示模式提示

### 单词本

语种 → 分类 → 词书 → 配置每日新学与复习比例，确认后写入云端。

### 我的

- 未登录：占位 +「微信一键登录」
- 已登录：头像、昵称、累计/连续学习天数、设置入口、退出登录
- 网络不通时顶部橙色横幅提示

### 设置

背词模式（看词选义 / 听词摸鱼）、拼读速度、单词重复次数；已登录时同步到服务端。

### 登录

与考证宝相同的 **微信一键登录**：

1. 用户点击「微信授权登录」
2. `wx.getUserProfile` 取头像昵称（须在点击手势里调用）
3. `wx.login` 取 `code`
4. `POST /api/user/auth/wechat/quick`，body 含 `code` 与 `userInfo`
5. 本地写入 `token`、`word_current_user`

未接微信开放平台时，openid 使用本机模拟值 `miniOpenid`（与考证宝一致），避免每次登录新建账号。

---

## 单词本接口（用户服务）

| 路径 | 说明 |
|------|------|
| `POST /user/word/book/list` | 启用中的单词本列表（可不登录） |
| `POST /user/word/book/current` | 当前词书 + 新学/复习进度（需登录） |
| `POST /user/word/book/switch` | 切换词书并写入 `user_word_current_book`（需登录） |
| `POST /user/word/study/session` | 本轮学习队列 |
| `POST /user/word/preference/get` | 用户偏好 |
| `POST /user/word/preference/update` | 更新偏好 |

用户进度表 DDL：`arelore-server-admin/src/main/resources/sql/arelore_education/word/user_word.sql`，需在 `arelore_education` 库执行后，切换才能落库。

---

## 本地数据

| Key | 含义 |
|-----|------|
| `word_current_book_id` | 当前单词本 **code** |
| `word_current_user` | 后端用户信息（`nickname` / `avatar` 等） |
| `token` | 登录凭证 |
| `miniOpenid` | 本地模拟 openid |
| `word_preference` | 本机背词偏好 |
| `miniApiEnv` / `miniApiBaseUrl` | 可选，覆盖接口环境 |

进度数字优先来自 `book/current`。退出登录不清当前词书 code。

---

## 和「考证宝」的区别

| | 背单词（本模块） | 考证宝 |
|--|------------------|--------|
| 目录 | `arelore-client-mini-word` | `arelore-client-mini-cert` |
| 实现 | **微信原生** | Taro + React |
| 打开方式 | 开发者工具导入本目录 | `bash start.sh [env]` 构建后导入（`miniprogramRoot` 为 `dist/`） |
| 切环境 | 写 `env.local.js` + 重新编译 | `TARO_APP_API_BASE_URL` 注入构建 |

不要把 Taro 工程的构建方式套到本模块。
