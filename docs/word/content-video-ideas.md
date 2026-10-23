# 短视频思路总表（含提示词 case）

面向：**视频号 → 小程序**；**抖音 → Web**。  
约束：Seedance / 豆包 / 即梦 / 小云雀等 **每日生成次数有限** → 优先「短镜头 3～8s + 剪映拼接 + 云 TTS」，整片一镜到底少用。

**额度用法建议**

| 优先级 | 用法 | 每次消耗 |
|--------|------|----------|
| P0 | 主线 A 合成/多义的「两个场景」各 1 段 | 2 段/条 |
| P1 | 短语场景、听写揭晓的 1 段氛围 | 1 段/条 |
| P2 | 账号统一片头 2s 环路（做一次复用很久） | 偶尔 |
| 省额度 | 主线 B 朗读条、对比字幕条 | **0 次视频模型**（静图即可） |

下列「视频提示词」按 **3～6 秒竖屏镜头** 写，可直接丢给文生视频；成片口播/字幕在剪映加。

---

## 总表

| # | 思路 | 是否你原有 | 导流相关度 | 耗视频额度 | 建议 | Case 提示词（示例，竖屏 9:16，写实或轻动画二选一） |
|---|------|------------|------------|------------|------|--------------------------------------------------|
| 1 | 单词讲解（拆词/场景合成） | ✓ 原有 | ★★★★★ | 中（2 段/条） | **主做** | 见下方 §1 |
| 2 | 短语/情景动画 | ✓ 原有 | ★★★☆☆ | 高 | 控量辅线 | 见 §2 |
| 3 | 少儿卡通故事 | ✓ 原有 | ★☆☆☆☆ | 高 | **不做** | 见 §3（仅作对照） |
| 4 | 英文毒鸡汤+混剪 | ✓ 原有 | ★★☆☆☆ | 高/或本地慢 | **不做主业** | 见 §4 |
| 5 | 单图+字幕+朗读 | ✓ 原有 | ★★★★☆ | **零** | **日更主做** | 见 §5（偏文案，非视频模型） |
| 6 | 听写揭晓（先听后字） | 新增 | ★★★★★ | 低 | **主做** | 见 §6 |
| 7 | 易混词分屏对比 | 新增 | ★★★★★ | 低～中 | **主做** | 见 §7 |
| 8 | 派生词「积木扣缀」 | 新增 | ★★★★★ | 低 | **主做** | 见 §8 |
| 9 | 一词多义双场景 | 新增 | ★★★★☆ | 中 | 每周 1～2 | 见 §9 |
| 10 | 发音口型特写 | 新增 | ★★★☆☆ | 中 | 偶尔 | 见 §10 |
| 11 | 通勤第一人称 POV | 新增 | ★★★★☆ | 中 | 系列感 | 见 §11 |
| 12 | 拼写错误→改正 | 新增 | ★★★★☆ | 低 | 互动好 | 见 §12 |
| 13 | 三词微型故事 | 新增 | ★★★☆☆ | 中 | 调剂 | 见 §13 |
| 14 | 中英对照「先猜后揭」 | 新增 | ★★★★☆ | **零～低** | 省额度 | 见 §14 |
| 15 | 考季倒计时条 | 新增 | ★★★★☆ | 低 | 考季加更 | 见 §15 |
| 16 | ASMR/环境音+单词 | 新增 | ★★★☆☆ | 中 | 夜间流量 | 见 §16 |
| 17 | 办公室微尴尬口语 | 新增 | ★★★☆☆ | 高 | 少做精品 | 见 §17 |
| 18 | 词源/文化一瞥 | 新增 | ★★★☆☆ | 中 | 差异化 | 见 §18 |

---

## §1 单词讲解（合成词等）— 原有思路 1

**怎么用额度**：词 A、词 B、合成结果可各 1 段，或 A+B 两段 + 静帧出成词。

| Case | 视频提示词 |
|------|------------|
| ice + cream | `Vertical 9:16, 4 seconds. Close-up of clear ice cubes in a glass, soft daylight, shallow depth of field, no text, no logo, cinematic, realistic.` |
| ice + cream（B） | `Vertical 9:16, 4 seconds. Soft serve ice cream cone, warm cafe bokeh background, appetizing, no text, realistic.` |
| note + book | `Vertical 9:16, 5 seconds. A hand writing a short note on sticky paper on a wooden desk, then a closed hardcover notebook beside it, natural light, no text.` |
| rain + coat | `Vertical 9:16, 5 seconds. Rain on a city sidewalk, then a person putting on a yellow raincoat, cozy mood, no face close-up needed, no text.` |
| sun + flower | `Vertical 9:16, 4 seconds. Bright sunflower field, gentle camera push-in, golden hour, no text, realistic.` |

口播仍按主线 A：先拆再合。

---

## §2 短语/情景 — 原有思路 2

**怎么用额度**：每条短语 **1 段情景** 即可，讲解用字幕，别拍成长剧。

| Case | 视频提示词 |
|------|------------|
| 问路 ask for directions | `Vertical 9:16, 6 seconds. Traveler with a small suitcase on a sidewalk politely gesturing to a local, city crosswalk, daylight, natural, no readable street signs in English, no text overlay.` |
| 点餐 order food | `Vertical 9:16, 5 seconds. Customer pointing at a cafe counter menu board (blurred text), barista nodding, warm interior, no readable words.` |
| 看病 see a doctor | `Vertical 9:16, 5 seconds. Clinic waiting area, person holding a number ticket, calm lighting, realistic, no logos, no text.` |
| 购物 check out | `Vertical 9:16, 5 seconds. Hands placing items on a supermarket checkout belt, soft motion, no brand logos, no text.` |

---

## §3 少儿卡通 — 原有思路 3（不推荐）

| Case | 说明 |
|------|------|
| 动物学堂对话 | 与账号人设（成人听写）冲突，审核与导流都差 → **跳过** |

若仅试验模型能力，可用但不发布到主号。

---

## §4 毒鸡汤混剪 — 原有思路 4（不推荐主业）

| Case | 视频提示词（仅当试验云端、勿作账号主线） |
|------|------------------------------------------|
| 励志空镜 | `Vertical 9:16, 5 seconds. Slow drone-like move over misty mountains at sunrise, epic calm, no text.` |
| 城市夜景 | `Vertical 9:16, 5 seconds. Timelapse feel of city lights from a high window, moody, no text.` |

文案与产品弱相关；额度优先让给 §1/§6/§9。

---

## §5 单图朗读 — 原有思路 5（零视频额度）

不调用视频模型。用 **文生图** 或图库 + TTS。

| Case | 文生图提示词（若需要） | 口播/字幕 |
|------|------------------------|-----------|
| 通勤 5 词封面 | `Vertical poster, minimal desk with headphones and coffee, soft morning light, clean negative space for big English title, no watermark.` | 三词朗读 |
| 听写 Day 封面 | `Simple calendar page with a large day number, pastel, vertical, blank area for text.` | Day N |
| 风景弱相关 | `Quiet lakeside at dusk, peaceful, vertical, cinematic still.` | 一句精听 |

---

## §6 听写揭晓 — 新增（强推）

成片：先黑场/模糊画面只出声音 → 再出清晰词形。视频模型只生成「揭晓瞬间」或全程静图。

| Case | 视频提示词 |
|------|------------|
| 揭晓瞬间 | `Vertical 9:16, 3 seconds. A chalkboard rapidly revealing a single English word written in large chalk letters: "BECAUSE", dust particles, school mood, only that word readable.` |
| 手机备忘录揭晓 | `Vertical 9:16, 3 seconds. Phone notes app mock UI blur then sharp focus on one word "QUEUE", realistic hand holding phone, no other readable text.` |
| 打字机弹出 | `Vertical 9:16, 4 seconds. Vintage typewriter typing one word "RECEIVE" letter by letter, warm lamp light, macro, cinematic.` |

---

## §7 易混词分屏 — 新增

| Case | 视频提示词 |
|------|------------|
| affect vs effect | `Vertical 9:16, 5 seconds. Split screen: left a person looking emotional (affect vibe), right falling dominoes (effect vibe), clean divider, no text on screen.` |
| desert vs dessert | `Vertical 9:16, 5 seconds. Split screen: left dry sand dunes, right a slice of cake on a plate, bright contrast, no text.` |
| lose vs loose | `Vertical 9:16, 4 seconds. Left: keys disappearing into a couch cushion; right: a loose screw wobbling; no text.` |

词形用剪映大字幕叠，省得模型把字烧进画面（难改）。

---

## §8 派生积木 — 新增

| Case | 视频提示词 |
|------|------------|
| un- 扣上 | `Vertical 9:16, 5 seconds. Wooden alphabet blocks: block "UN" slides and snaps onto blocks "HAPPY", stop-motion style, bright desk, no extra text.` |
| -tion 变形 | `Vertical 9:16, 5 seconds. Soft 3D letters "ACT" morph smoothly into "ACTION", clean white studio, product-video look, no logos.` |
| -ful / -less | `Vertical 9:16, 5 seconds. Two doors labeled only with icons (smile vs empty), then wooden stamps "FUL" and "LESS" pressing on paper, no English sentence text.` |

---

## §9 一词多义双场景 — 新增

| Case | 视频提示词 |
|------|------------|
| light=灯 | `Vertical 9:16, 4 seconds. Fingers turning on a warm bedside lamp in a dark room, cozy, no text.` |
| light=轻 | `Vertical 9:16, 4 seconds. Hand lifting a feather-light paper bag easily, daylight kitchen, no text.` |
| bank=银行/河岸 | `Vertical 9:16, 4 seconds. Glass bank building exterior sunny day.` / `Vertical 9:16, 4 seconds. Quiet river bank with grass, peaceful.` |
| watch=看/表 | `Vertical 9:16, 4 seconds. Eyes watching a street performance.` / `Vertical 9:16, 4 seconds. Wristwatch close-up ticking, macro.` |

---

## §10 口型/发音 — 新增

| Case | 视频提示词 |
|------|------------|
| th 音口型 | `Vertical 9:16, 4 seconds. Close-up of adult mouth clearly pronouncing English "TH" sound, soft studio light, educational, respectful, no exaggerated comedy.` |
| 重音示意 | `Vertical 9:16, 4 seconds. Abstract sound waveform pulsing beside a silhouette speaking, clean tech aesthetic, no readable words.` |

注意：真人脸/口型生成易怪，可改用波形+字幕方案省额度。

---

## §11 通勤 POV — 新增

| Case | 视频提示词 |
|------|------------|
| 地铁耳机 | `Vertical 9:16, first-person POV, 5 seconds. Looking down at phone in a subway car, wearing earphones, blurred passengers, handheld subtle shake, realistic, screen content unreadable.` |
| 工位摸鱼感（勿用摸鱼文案） | `Vertical 9:16, first-person POV, 5 seconds. Office desk, coffee and keyboard, gaze briefly out the window, calm, no logos, screen unreadable.` |
| 排队 | `Vertical 9:16, POV, 4 seconds. Waiting in a short cafe queue, phone in hand, soft noise, realistic.` |

口播：「三分钟听一组词」→ 接 B 或 A。

---

## §12 错拼→改正 — 新增

| Case | 视频提示词 |
|------|------------|
| 红笔改正 | `Vertical 9:16, 4 seconds. Notebook page with a misspelled word crossed by red pen, then correct spelling written beside it, top-down, natural light. Show wrong as "RECIEVE" then correct "RECEIVE".` |
| 橡皮改错 | `Vertical 9:16, 4 seconds. Pencil erasing a wrong English word on paper, rewriting the correct one "NECESSARY", student desk aesthetic.` |

---

## §13 三词微故事 — 新增

| Case | 视频提示词 |
|------|------------|
| lost / map / home | `Vertical 9:16, 6 seconds. Short silent story: person confused at a crossroads, unfolds a paper map, then walks toward a warm lit doorway at dusk, cinematic, no text.` |
| rain / coat / late | `Vertical 9:16, 6 seconds. Person rushing in rain, zips raincoat, checks watch anxiously, city evening, no text.` |

片尾字幕列出三词，导向听写。

---

## §14 先猜后揭 — 新增（省额度）

视频模型可选 0：静图中文场景 → 揭晓英文。

| Case | 文生图或视频提示词 |
|------|-------------------|
| 猜「排队」 | `Vertical still: people standing in a neat line at a ticket window, anonymous, no signs in English.` → 字幕揭晓 `queue` |
| 猜「截止日期」 | `Vertical still: calendar with a red circle on a date, desk lamp, no English text.` → `deadline` |

---

## §15 考季倒计时 — 新增

| Case | 视频提示词 |
|------|------------|
| 倒计时氛围 | `Vertical 9:16, 4 seconds. Desk with exam admission ticket blurred, countdown flip clock showing 30 days, tense but hopeful morning light, no readable personal info.` |
| 刷题桌面 | `Vertical 9:16, 4 seconds. Highlighter and vocabulary book closed (cover unreadable), coffee, sunrise by window.` |

---

## §16 ASMR 环境音 — 新增

| Case | 视频提示词 |
|------|------------|
| 雨声窗边 | `Vertical 9:16, 6 seconds. Rain on window glass at night, cozy room reflection, slow move, no text, ASMR visual.` |
| 翻书 | `Vertical 9:16, 5 seconds. Macro of fingers slowly turning book pages, soft lamp, calming.` |

声音轨：环境音 + 单词 TTS；适合夜间发。

---

## §17 办公微情景口语 — 新增（费额度，少做）

| Case | 视频提示词 |
|------|------------|
| 会议迟到 | `Vertical 9:16, 6 seconds. Person quietly entering a meeting room looking apologetic, colleagues glance up, realistic office, no logos, no readable slides.` |
| 请同事帮忙 | `Vertical 9:16, 5 seconds. Cubicle, one colleague leans over politely asking something, friendly, no text.` |

---

## §18 词源/文化一瞥 — 新增

| Case | 视频提示词 |
|------|------------|
| salary ↔ salt 传说向 | `Vertical 9:16, 5 seconds. Artistic shot of sea salt crystals and old coins on dark wood, museum mood, no text.` |
| sandwich | `Vertical 9:16, 5 seconds. Hands assembling a simple sandwich, cafe table, appetizing, no brand.` |

讲解用旁白讲清「故事→词」，避免伪科学过度承诺。

---

## 每日有限次数时怎么排

假设 **每天仅 3～5 次** 文生视频：

| 日类型 | 额度分配 | 发布 |
|--------|----------|------|
| A 日（讲解） | 2 次给双场景（合成/多义）+ 1 次备用 | 1 条 A + 可选 1 条 B（B 不耗额度） |
| B 日（朗读） | **0 次** 视频模型 | 1～2 条 B |
| 互动日 | 1 次给听写揭晓或错拼改正 | 1 条 §6/§12 |
| 存片日 | 3～5 次全打场景空镜，周末剪 | 不发布，只备料 |

**不要**把有限次数花在少儿卡通和毒鸡汤空镜上。

---

## 与文档关系

- 主线结构：[content-mainline-a.md](./content-mainline-a.md) / [content-mainline-b.md](./content-mainline-b.md)  
- 周节奏：[content-production.md](./content-production.md)  
- 本文：思路扩展 + 可复制提示词
