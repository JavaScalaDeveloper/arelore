# 背单词微信小程序（原生）

模块路径：`arelore-client/arelore-client-mini/arelore-client-mini-word`

用 **微信原生小程序**（WXML / WXSS / JS）实现的背单词壳，**不使用 Taro**。当前版本把底部导航、单词本切换、新学/复习进度卡片、**微信一键登录**跑通，词本列表走用户服务接口。新学/复习刷词流程尚未接入。

| 项 | 值 |
|------|------|
| 名称 | 背单词 |
| 技术栈 | 微信原生小程序，无 npm / 无 Taro |
| 最低基础库 | 建议 2.10.4+（`getUserProfile`） |
| 登录 | 微信一键登录（`getUserProfile` + `wx.login` + `/api/user/auth/wechat/quick`） |

---

## 已实现功能

1. **底部两个菜单**：`单词`、`我的`（原生 `tabBar`）。
2. **单词页顶部**展示当前单词本；点击进入单词本列表，可切换。
3. **单词页中间两张卡片**：`新学`、`复习`，各自展示 **待完成 / 已完成** 数量（随单词本切换）。
4. **我的页**：顶部展示当前微信用户头像、昵称；已登录时有 **退出登录**。

新学 / 复习卡片目前只提示「流程稍后接入」，不进入刷词页。

---

## 页面结构

```
arelore-client-mini-word/
├── app.js / app.json / app.wxss
├── project.config.json
├── sitemap.json
├── assets/                 # tabBar 图标
├── utils/
│   ├── mock.js             # 示例单词本与进度
│   ├── storage.js          # 词书、token、用户
│   ├── config.js           # 接口根地址
│   └── request.js          # 登录接口请求
└── pages/
    ├── words/              # 单词（tab）
    ├── profile/            # 我的（tab）
    ├── books/              # 切换单词本
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

## 页面说明

### 单词

- 顶栏：当前单词本名称 +「切换」
- 点击顶栏 → `pages/books/index`
- 蓝色卡片 **新学**：待完成、已完成
- 紫色卡片 **复习**：待完成、已完成

### 单词本

内置示例：四级、六级、考研、雅思。点选后写回本地存储，返回单词页即更新名称和数量。

### 我的

- 未登录：展示占位信息 +「去登录」
- 已登录：头像、昵称、「退出登录」（清本地用户，不改单词本选择）

### 登录

与考证宝相同的 **微信一键登录**：

1. 用户点击「微信授权登录」
2. `wx.getUserProfile` 取头像昵称（须在点击手势里调用）
3. `wx.login` 取 `code`
4. `POST /api/user/auth/wechat/quick`，body 含 `code` 与 `userInfo`
5. 本地写入 `token`、`word_current_user`

默认接口根地址：`http://localhost:8281/api`（对应本地 user 服务默认 `spring.profiles.active=test`）。

| 环境 | user-api | Storage `miniApiEnv` |
|------|----------|----------------------|
| prd  | 8081     | `prd`                |
| pre  | 8181     | `pre`                |
| test | 8281     | `test`（默认）       |
| dev  | 8381     | `dev`                |

开发者工具 Storage 可写 `miniApiBaseUrl`（完整地址）或 `miniApiEnv` 覆盖。请先启动用户服务，并保持 `project.config.json` 里 `urlCheck: false`。

未接微信开放平台时，openid 使用本机模拟值 `miniOpenid`（与考证宝一致），避免每次登录新建账号。

### 单词本接口（用户服务，本地默认 8281）

| 路径 | 说明 |
|------|------|
| `POST /user/word/book/list` | 启用中的单词本列表（可不登录） |
| `POST /user/word/book/current` | 当前词书 + 新学/复习进度（需登录） |
| `POST /user/word/book/switch` | 切换词书并写入 `user_word_current_book`（需登录） |

用户进度表 DDL：`arelore-server-admin/src/main/resources/sql/arelore_education/word/user_word.sql`，需在 `arelore_education` 库执行后，切换才能落库。

---

## 本地数据

| Key | 含义 |
|-----|------|
| `word_current_book_id` | 当前单词本 **code** |
| `word_current_user` | 后端用户信息（`nickname` / `avatar` 等） |
| `token` | 登录凭证 |
| `miniOpenid` | 本地模拟 openid |

进度数字优先来自 `book/current`；未登录时用词本 `wordCount` 作为新学待完成。退出登录不清当前词书 code。

---

## 和「考证宝」的区别

| | 背单词（本模块） | 考证宝 |
|--|------------------|--------|
| 目录 | `arelore-client-mini-word` | `arelore-client-mini-cert` |
| 实现 | **微信原生** | Taro + React |
| 打开方式 | 开发者工具导入本目录 | 先 `npm run dev:weapp` 再导入（`miniprogramRoot` 为 `dist/`） |

不要把 Taro 工程的构建方式套到本模块。

---

## 后续可接

- 每日新学 / 复习刷词队列（`user_word_learn_record`）
