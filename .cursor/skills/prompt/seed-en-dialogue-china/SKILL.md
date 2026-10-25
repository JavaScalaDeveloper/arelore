---
name: seed-en-dialogue-china
description: >-
  Demo pattern for generating user_ai_prompt seed INSERT SQL for Doubao-style
  China English-dialogue short videos. Use when writing or regenerating
  seed_en_dialogue_china_*.sql, 英语情景对话种子, or CONFIG-tag prompt batches under
  arelore_creator/prompt.
---

# 英语情景对话种子（demo）

批量写 `user_ai_prompt` / `user_ai_prompt_tag` 种子时，按下面约定；**不要**把整库对话都塞进 skill——用脚本或对话生成正文，skill 只定规则 + demo。

## 规则（必守）

| 项 | 约定 |
|----|------|
| 组合 | `大类 × 子场景 × 2 主角`（例：10×10×2=200） |
| uuid | `md5("{batchId}\|{protagonist}\|{category}\|{sub}")` 小写 hex |
| title | **短**引流（建议 ≤15 字级），含「英语」，**不要**带 `#话题`，全局唯一 |
| summary | 独立引流文案，从**对话场景**写钩子（在哪/会说什么），**必须含**两个 `#话题`；与 title 不同。禁止：竖屏/秒数/字幕等制作规格；禁止写主角名（如「小女孩」）或「主角「…」」 |
| config_tags | `["抖音","视频号","小红书","英语","英语情景对话",大类,主角]` |
| manual_tags | `[]`；`publish_status=DRAFT`；`status=NORMAL` |
| prompt | 9:16 / 15-20s / 上英下中 / **仅英音** / 中国场景可见；SQL 内 `'` → `''` |
| 形象 | 同一主角**不要**全篇同一套穿着；发型/衣着/配饰按条轮换（年龄段可保留） |

`ext_info` 至少含：`summary`、`importBatchId`、`sceneCategory`、`sceneSub`、`protagonist`、`aspectRatio`、`audio`。

## Demo：生成一条 INSERT

```bash
python3 .cursor/skills/prompt/seed-en-dialogue-china/scripts/generate_seed.py
```

输出示例结构（完整可跑片段见脚本）：

```sql
USE arelore_creator;
SET @user_id = 26882207331096889723;

INSERT INTO `user_ai_prompt_tag` (`user_id`,`tag_type`,`name`,`color`,`sort_order`,`status`) VALUES
(@user_id,'CONFIG','英语','blue',40,'NORMAL')
ON DUPLICATE KEY UPDATE `color`=VALUES(`color`),`sort_order`=VALUES(`sort_order`),`status`=VALUES(`status`);

INSERT INTO `user_ai_prompt`
(`user_id`,`prompt_uuid`,`title`,`prompt_text`,`config_tags`,`manual_tags`,`publish_status`,`status`,`ext_info`)
VALUES
(@user_id,'<md5>',
 '小女孩·公交车英语🚌',           -- 短标题，无 #
 '生成要求：9:16…（省略）',
 CAST('["抖音","视频号","小红书","英语","英语情景对话","问路","小女孩"]' AS JSON),
 CAST('[]' AS JSON),'DRAFT','NORMAL',
 CAST('{"summary":"问路不会开口？站台这一句 Excuse me 先救场。听感干净… #英语口语 #生活英语","importBatchId":"en-cn-dialogue-china-demo",...}' AS JSON)
);
```

批量种子落盘路径：

`arelore-server/arelore-server-admin/src/main/resources/sql/arelore_creator/prompt/seed_en_dialogue_china_{N}.sql`

只写 `INSERT ... ON DUPLICATE KEY UPDATE`（标签按 `uk_user_id_tag_type_name`，提示词按 `uk_user_id_prompt_uuid`），**不要** `DELETE`。
