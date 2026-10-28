# 墨鱼背词 / 背单词（word）

碎片时间英语词汇学习产品：词书 + 每日新学/复习计划，支持看词识意、听力练习、听写等模式。

对外品牌以 **墨鱼背词** 为主；微信小程序展示名多为 **背单词**；浏览器标签页使用中性标题（如「墨鱼工作台」），避免旁人一眼看出在背单词。

## 客户端与入口

| 端 | 路径 / URL | 说明 |
|----|------------|------|
| Web | `https://www.arelore.com/word`（亦支持 `/zh-cn/word`、`/en-us/word`） | React；未登录可见 SEO 落地页 |
| 微信小程序 | `arelore-client/arelore-client-mini/arelore-client-mini-word` | 原生小程序；词书、学习、设置、微信登录 |
| 管理端 | Admin → 产品「背单词」 | 语种 / 分类 / 词书 / 词条、TTS 等 |

小程序能力与本地调试见：`arelore-client-mini-word/README.md`。

## 核心能力（摘要）

- 词书切换与学习计划（每日新学、复习比例、发音偏好、复习回溯与断签软着陆）
- 学习模式：看词识意、听词（摸鱼）、听写；看词选图规划中
- Web / 小程序偏好与 TTS（单词、拼读、释义）
- 邮箱验证码登录（Web）；微信一键登录（小程序）

## 文档

| 文档 | 内容 |
|------|------|
| [推广计划](./promotion-plan.md) | Web / 小程序推广、引流、SEO、阶段节奏与指标 |
| [主线 A · 单词讲解](./content-mainline-a.md) | 视频号→小程序、抖音→Web；合成词/派生词等片型与两周选题 |
| [主线 B · 朗读听力](./content-mainline-b.md) | 通勤听写 / 一句精听 / 听写挑战；低成本日更 |
| [内容生产手册](./content-production.md) | A+B 周节奏、备料、发布清单、Seedance 使用边界 |
| [视频思路与提示词](./content-video-ideas.md) | 原有 5 种 + 新增思路总表；Seedance/即梦等限次用法与 case |
| [竞品痛点调研](./competitive-pain-points.md) | 主流背单词 App/小程序/网站痛点与差异化启示 |
| [产品优化建议](./product-optimization.md) | 结合 DeepSeek 与本仓分析的墨鱼背词优化清单 |
| [复习债软着陆](./review-soft-land.md) | 回溯池 + 每日上限 + 断签压缩；计划 `ext_info` 字段 |
| [词典富化](./dict-enrichment.md) | 有道同步例句/词组/近义/派生 → 学习卡展示 |

## 相关代码（便于对照）

- Web SEO：`arelore-client-web/src/pages/word/seo/`
- Web 落地：`WordSeoLanding.tsx`、`public/robots.txt`、`public/sitemap.xml`
- 小程序：`arelore-client-mini-word/`
