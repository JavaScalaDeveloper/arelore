# 小程序端应用

当前有两个独立小程序，**技术栈不同，不要混用启动方式**。

## 项目列表

| 模块 | 说明 | 技术栈 |
|------|------|--------|
| [arelore-client-mini-word](./arelore-client-mini-word) | 背单词：词书切换、新学/复习进度、微信头像登录 | **微信原生**（无 Taro） |
| [arelore-client-mini-cert](./arelore-client-mini-cert) | 考证宝：刷题、收藏、考试记录 | Taro + React |

## 背单词（原生）

微信开发者工具直接导入 `arelore-client-mini-word` 目录即可，无需 `npm install`。详见该目录 README。

## 考证宝（Taro）

```bash
cd arelore-client-mini-cert
npm install
npm run dev:weapp
```

再用开发者工具导入 `arelore-client-mini-cert`（`miniprogramRoot` 指向 `dist/`）。
