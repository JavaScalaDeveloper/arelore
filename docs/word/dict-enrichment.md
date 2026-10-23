# 词典富化（有道同步 → 学习卡）

## 管理端「从词条同步」

1. 扫描词条词形写入 `admin_word_base_info`  
2. 对 **缺释义或缺例句** 的词调用有道 jsonapi，写入 `ext_info`（不覆盖已有配图与已有字段）：

| 字段 | 来源 | 用途 |
|------|------|------|
| meanings / ukphone / usphone | ec / simple | 释义、音标 |
| sentence.sentences | blng_sents_part（auth 兜底） | 双语例句 |
| phrases | phrs | 词组 |
| synonyms | syno | 近义 |
| relatedWords | rel_word | 派生 |
| etym | etym 摘要 | 词源 |

标签：`has_example` / `no_example`（另有 has_meaning 等）。

## 学习端

词书 `admin_word_entry` 优先；例句/释义缺失时回退 `base_info`。  
揭晓后展示：例句、词组、近义、派生、词源（听写模式不刷屏，主要展示释义）。

## 使用

1. 管理端「单词基础信息」→ **从词条同步**（词量大、有道限速，耗时会较长）  
2. 列表可用标签筛选 **有例句 / 无例句**  
3. 详情页可查看例句、词组、近义、派生、词源  
4. 同步后再背词（Web / 小程序揭晓后可见富化内容）
