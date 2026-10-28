---
name: seed-en-dialogue-china
description: >-
  Generate high-quality user_ai_prompt seed SQL for Doubao-style China English
  dialogue short videos. Use when the user asks for N 条英语情景对话种子,
  seed_en_dialogue_china_*.sql, or arelore_creator prompt batches. Count N comes
  from the user message — do not rely on fixed batch Python scripts.
---

# 英语情景对话种子

按用户指定的 **条数 N** 直接生成 SQL（Agent 写文件），**不要**维护/调用一堆 `generate_batch_*.py`。保质保量优先于堆数量。

## 触发与条数

用户说「生成 N 条 / 再来 N 条 / seed_en_dialogue_china_N」→ 产出恰好 **N** 条 `user_ai_prompt`。

| 未说明 | 处理 |
|--------|------|
| 未给 N | 先问要几条，或默认 20 条 demo |
| 只要改规则/样例 | 只改本 skill / 少量示例，不重写整库 |
| 未提标签 | **默认不写** `user_ai_prompt_tag` INSERT（库里已有 CONFIG 即可） |
| 明确要同步标签 | 再补 tag UPSERT |

落盘：

`arelore-server/arelore-server-admin/src/main/resources/sql/arelore_creator/prompt/seed_en_dialogue_china_{N}.sql`

`importBatchId`：`en-cn-dialogue-china-{N}`（或用户指定的 batch 名）。

## 场景规划（先算再写）

`N = 场景数 × 每场景主角数`。默认每场景 **2** 个不同主角。

| N | 建议拆法（可调） |
|---|------------------|
| ≤20 | 10 子场景 × 2 主角，或 5×2×2 大类 |
| 50～200 | 约 `ceil(N/2)` 个互不雷同的子场景 |
| 300+ | 多扩**大类**与**子场景**，禁止复制旧批骨架只改称呼 |

大类池（按需取用，可增）：问路、点餐、购物、交通出行、酒店住宿、就医问诊、校园生活、银行邮政、日常服务、家庭互助、快递外卖、运动健身、休闲娱乐、职场办公、旅行游玩、政务办事、亲子教育、数字生活、节日庆典、邻里社区、美妆护理、汽车服务、学习考试、慈善志愿、农业市集。

主角池（10）：小女孩、小男孩、少女、少年、年轻女性、年轻男性、中年女性、中年男性、老奶奶、老爷爷。同场景两主角须不同；批次内尽量轮换。

## 字段约定（必守）

| 项 | 约定 |
|----|------|
| uuid | `md5("{batchId}\|{protagonist}\|{category}\|{sub}")` 小写 hex |
| title | 短引流（约 ≤15 字），含「英语」，**无 `#`**，全文件唯一 |
| summary | 场景钩子 + **至少两个 `#话题`**；禁竖屏/秒数/字幕规格；**禁写主角名** |
| config_tags | `["抖音","视频号","小红书","英语","英语情景对话",大类,主角]` |
| manual_tags | `[]`；`publish_status=DRAFT`；`status=NORMAL` |
| prompt | 9:16、15–20s、上英下中、**仅英音**、中国场景可见；SQL 中 `'` → `''` |
| ext_info | 至少：`summary`、`importBatchId`、`sceneCategory`、`sceneSub`、`protagonist`、`aspectRatio`、`audio` |

只写 `INSERT ... ON DUPLICATE KEY UPDATE`（按 `uk_user_id_prompt_uuid`），**禁止 DELETE**。

`user_id` 默认：`26882207331096889723`（`SET @user_id = ...`）。

## 质量红线（保质）

写完前必须自检；不达标就改，不准凑数交差。

1. **对话内容**  
   - 同场景两个主角：**两套完全不同的英文台词**（禁止只改说话人名字）。  
   - **英文句级全局唯一**：任意一句英文台词不得在文件中出现第二次。  
   - 英文里**禁止夹中文**大类/子场景名（禁止 `Let me check the 问路 list` 这类）。  
   - 中文括号释义可含「子场景」语义，但英文必须自然、可跟读、贴合该子场景（数字/门牌/份量等细节按条变化）。  
   - 每条建议 8～10 轮对白（P/S 交替），够 15–20 秒。

2. **形象**  
   - 同一主角多条之间：**发型 / 衣着 / 配饰 / 气质**组合不重复。  
   - 年龄段与主角匹配（如小女孩约 6–10 岁；中年男性约 40–50 岁）。  
   - 禁止全文件共用「马尾辫配粉色发卡，浅色短袖连衣裙」这类固定句。

3. **title / summary**  
   - title、summary 各自全文件唯一；summary ≠ title；summary 不含主角称呼。

4. **反面教材**  
   - 旧文件 `seed_en_dialogue_china_200.sql`：大量「场景不同、台词相同」——**禁止再仿**。  
   - 勿用固定模板只替换 `{sub}`/`{cat}` 中文进英文。

## prompt 正文结构（每条）

按此骨架填写，对话段必须原创：

```
生成要求：9:16竖屏，15-20秒。{中国场景地点}。语速明快。风格：{风格}。

画面描述：{主角}（{年龄与形象，本条独有}）正在「{大类}/{子场景}」…配角为{配角}。…中国可见…结尾友好收束。

声音要求：语音仅使用英文。{主角}声音{音色}；{配角}声音自然清晰、符合身份。

文本与字幕要求：…上英下中…中文必须为简体。

对话文案（语音仅读英文）：

{主角}: {English} ({简体中文})
{配角}: ...
…

【输出】
输出一条完整的15～20秒竖屏成片，已按要求烧录「上英下中」字幕，可直接用于发布。
```

## SQL 形态（示例一条）

```sql
USE arelore_creator;
SET @user_id = 26882207331096889723;

INSERT INTO `user_ai_prompt`
(`user_id`,`prompt_uuid`,`title`,`prompt_text`,`config_tags`,`manual_tags`,`publish_status`,`status`,`ext_info`)
VALUES
(@user_id,'<md5 hex>',
 '小女孩·共享单车英语🚲',
 '生成要求：9:16…（完整 prompt，单引号已加倍）',
 CAST('["抖音","视频号","小红书","英语","英语情景对话","问路","小女孩"]' AS JSON),
 CAST('[]' AS JSON),'DRAFT','NORMAL',
 CAST('{"summary":"共享单车扫码不会？先问开锁怎么说。#英语口语 #生活英语","importBatchId":"en-cn-dialogue-china-20","sceneCategory":"问路","sceneSub":"共享单车站","protagonist":"小女孩","durationSec":"15-20","aspectRatio":"9:16","audio":"EN only","for":"doubao"}' AS JSON))
ON DUPLICATE KEY UPDATE
 `title`=VALUES(`title`),`prompt_text`=VALUES(`prompt_text`),`config_tags`=VALUES(`config_tags`),
 `manual_tags`=VALUES(`manual_tags`),`publish_status`=VALUES(`publish_status`),`status`=VALUES(`status`),`ext_info`=VALUES(`ext_info`);
```

大批量时：文件头注释写清 N、batchId、质量约定；`VALUES` 多行用逗号连接，末尾一条接 `ON DUPLICATE KEY UPDATE`。

## 交付自检（Agent 必做）

1. 条数 = N。  
2. 抽查：同场景两主角英文对白是否明显不同。  
3. 扫描：英文行是否含汉字；是否出现重复英文整句。  
4. 抽查同主角形象是否撞车。  
5. 需要时可用一次性本地命令做指纹统计（**不要**为此新增常驻 batch 脚本）：

```bash
# 可选：统计「对话文案」英文指纹是否唯一
python3 -c "..."
```

## 工作方式

- **直接写 SQL 文件**（或分段写入再合并），用对话/推理保证多样性与贴场景。  
- **禁止**为每个 N 新增 `generate_batch_{N}.py` 并当作唯一生成手段。  
- 若 N 很大（如 500+）：可分多轮写同一文件，每轮仍遵守句级唯一与形象不重复；宁可慢，不可糊。
