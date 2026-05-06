---
name: ruankao-exam-to-detection-sql
description: Parse software-exam question files (doc/docx/pdf), remove header/footer noise, and generate MySQL INSERT SQL for user_detection_type and user_detection_question tables. Supports optional standard answer, explanation, and scoring metadata when the source document provides them. Use when user asks to convert exam papers into detection SQL.
---

# Ruankao Exam To Detection SQL

## 适用场景

- 用户给出软考/题库文件（`doc`/`docx`/`pdf`）并要求转成检测题 SQL。
- 目标表为：
    - `arelore_education.user_detection_type`
    - `arelore_education.user_detection_question`
- **若原始文档中包含标准答案、试题解析**：应写入题库 JSON，便于小程序「查看解析」与交卷后服务端按配置算分。
- **若文档中不存在答案或解析**：对应字段省略或置空，不强行编造。

## 输出目标

1. 生成 1 条 `user_detection_type` 的 `INSERT` 语句（`ON DUPLICATE KEY UPDATE`）。
2. 生成 N 条 `user_detection_question` 的 `INSERT` 语句（每题一条，同上幂等）。
3. `user_detection_question.type_code` 必须等于检测项目 `type_code`。
4. 满足唯一约束：
    - `user_detection_type`: `type_code`
    - `user_detection_question`: `(type_code, question_code)`

## 执行流程

复制并执行此检查清单：

```text
- [ ] 读取原始文件文本
- [ ] 若有 docx 正文图片：解析关系与 media，计算 SHA-256，生成 common_binary_file SQL，并在题干/解析中写入 【img:…】 与 extra_info.imageHashValues
- [ ] 清理页眉页脚与页码噪音
- [ ] 抽取题目、选项；若有则抽取标准答案与解析
- [ ] 若有客观题标准答案：为选项配置分值并写入检测类型 `extra_info.scoring`（见下文「算分逻辑」）
- [ ] 生成 detection_type 的 INSERT
- [ ] 生成 detection_question 的 INSERT
- [ ] 校验 question_code 唯一、排序连续
- [ ] 输出可直接执行 SQL（UTF-8）
```

## 1) 文件读取

- `pdf`：优先直接读取文本；若有乱码/结构错乱，再逐页抽取并重排。
- `docx`：优先从 `word/document.xml` 抽取段落文本（与 Word 展示顺序一致）；必要时再合并表格/文本框。
- `doc`：先转换为 `docx`/纯文本再解析（保持原文顺序）。

## 2) 页眉页脚清理规则

忽略重复出现且不属于题干/选项的行，例如：

- 固定标题（每页重复）
- 页码行（如 `第 X 页`、`Page X`、`- 12 -`）
- 时间/版权水印行

建议规则：

- 若某行在全文出现频率异常高且长度短（如 < 30 字），优先判定为页眉页脚噪音。
- 仅删除“重复噪音行”，不要删除题干中的重复术语。

### 2.1 出版说明类括注（题干/选项/解析）

若原文在题目相关文本中夹带 **与题意无关的套话**（如 `（答案详解）`、`(答案详解)`、`【答案详解】`、`（试题详解）` 等），生成 SQL 前应 **整段删除该括注**，其余字句保留；**不要**改动 `【答案】` / `【解析】` 等解析用标记本身。检测类型 `type_name` / `type_description` 中也不要保留「（答案详解）」「来源：答案详解」等字样（`extra_info.source` 仍可保留原始文件名便于溯源）。

## 3) 题目结构抽取

每题至少包含：

- `question_order`：题号顺序（1..N）
- `question_name`：题干（`varchar(255)`；超长时截断，完整内容写入 `extra_info.fullQuestionName`）
- `question_description`：题型说明（客观题建议固定 `单选题`；心理量表类可沿用 `请选择最符合你的一项`）
- `options`：JSON 数组（A/B/C/D…）

### 3.1 选项 JSON（心理/维度类，与历史题库兼容）

```json
{
  "key": "A",
  "text": "选项文本",
  "scores": { "R": 1 },
  "dimension": "R"
}
```

若原题未明确维度/分值：将 `scores` 置为 `{}`，`dimension` 置为 `null`，并在 `extra_info` 标注 `"scorePending": true`。

### 3.2 标准答案与解析（软考客观题，**有则写入**）

当文档中存在标准答案、解析（例如 `【答案】B`、`【解析】……`）时，在 **`user_detection_question.extra_info`**（JSON）中写入（字段名保持一致，便于小程序与后端读取）：

| 字段 | 说明 |
|------|------|
| `questionType` | 如 `单选题` |
| `sourceQuestionNo` | 原始题号 |
| `correctOptionKey` | 单选标准答案键，如 `B` |
| `explanation` | 解析正文（可截断；超长完整内容放 `fullExplanation`） |
| `fullQuestionName` | 题干超长时与 `question_name` 规则一致 |
| `fullExplanation` | 解析超长时的完整文本 |
| `imageHashValues` | 本题关联的图片 SHA-256 十六进制串数组（小写），与题干/解析中的 `【img:…】` 占位符一一对应；无图则为 `[]` |

**若无【答案】/【解析】**：不要写 `correctOptionKey` / `explanation`，或显式置 `null`；不要杜撰。

**文档内嵌图片（与 `sql/arelore_education/软考/软考_软件设计师_2016下半年_上午_导入.sql` 一致）**：

- 从 `docx` 正文中抽取图片二进制（如 `word/media/*`，经 `document.xml` 中 `a:blip` / `r:embed` 解析），计算 **SHA-256**，写入 **`arelore_common.common_binary_file`**：`hash_value` 使用 `UNHEX(小写十六进制)`，`file_data` 使用 `0x` 十六进制字面量，`status=1`，`ext_json` 建议包含 `sourceDoc`（原始文件名）、`sha256`、`size`（字节数）。
- `INSERT` 使用 `ON DUPLICATE KEY UPDATE`（与参考 SQL 相同：`modify_time`、`status`、`ext_json`），依赖 `uk_hash_status` 幂等。
- 在题干或解析中保留占位 **`【img:64位小写hex】`**（与 `imageHashValues` 中字符串相同），便于客户端按 hash 拉取二进制并渲染。
- 生成 SQL 顺序：`SET NAMES utf8mb4` → **若有图片则输出 `common_binary_file` 段** → `USE arelore_education` → `user_detection_type` → `user_detection_question`。

### 3.3 客观题选项分值（**有标准答案且需交卷算分时**）

在 `options` 中为每个选项增加 `scores`，用于服务端通用评分累加（示例维度键统一为 `score`）：

- 正确选项：`"scores": { "score": 1 }`
- 错误选项：`"scores": { "score": 0 }`

同一行内连续多小题共用一行【答案】（如 `B C`）时，应拆成两道题，每题各自 `correctOptionKey` 与选项 JSON。

## 4) SQL 生成规范

### 4.1 检测项目（1 条）

使用如下结构（`extra_info` 为 JSON 字符串）：

```sql
INSERT INTO arelore_education.user_detection_type
(type_code, type_name, type_description, extra_info)
VALUES
('TYPE_CODE', '检测名称', '检测说明', '{"scoring":{"resultMode":"objective_sum","scoreDimension":"score","maxScore":75,"scoringVersion":"1.0"},"exam":"...","questionCount":75}')
ON DUPLICATE KEY UPDATE
                     type_name = VALUES(type_name),
                     type_description = VALUES(type_description),
                     extra_info = VALUES(extra_info);
```

#### 4.1.1 算分逻辑（`user_detection_type.extra_info.scoring`，**有客观题标准答案时配置**）

- `resultMode`: 客观卷求和推荐使用 **`objective_sum`**（将各题所选选项的 `scores.score` 累加，结果形如 `得分/满分`）。
- `scoreDimension`: 与选项 JSON 中维度键一致，默认 **`score`**。
- `maxScore`: 满分（通常等于题目数量，或试卷满分）。
- `scoringVersion`: 版本号字符串，如 `1.0`。

若试卷不需要服务端算分（例如纯心理量表），仍可使用 `pair_compare` / `max_score` 等模式，与 `options` 中 `scores`/`dimension` 设计一致即可。

**若文档无法提供可靠标准答案**：不要配置 `objective_sum` 与选项分值，避免错误判分；可保留 `scorePending` 或仅输出题干选项。

### 4.2 题目（N 条）

使用如下结构：

```sql
INSERT INTO arelore_education.user_detection_question
(type_code, question_code, question_name, question_order, question_description, options, extra_info)
VALUES
('TYPE_CODE', 'TYPE_CODE_Q001', '题干…', 1, '单选题',
 '[{"key":"A","text":"…","scores":{"score":0}},{"key":"B","text":"…","scores":{"score":1}}]',
 '{"questionType":"单选题","sourceQuestionNo":1,"correctOptionKey":"B","explanation":"……"}')
ON DUPLICATE KEY UPDATE
                     question_name = VALUES(question_name),
                     question_order = VALUES(question_order),
                     question_description = VALUES(question_description),
                     options = VALUES(options),
                     extra_info = VALUES(extra_info);
```

`question_code` 规则建议：

- 与 `type_code` 同一前缀并保证唯一，例如 `{type_code}_Q001`…`Q075`。
- 保证同一 `type_code` 内唯一。

## 5) 唯一索引冲突处理（必须实现）

输出 SQL 时优先使用幂等写法：

- `user_detection_type`：`INSERT ... ON DUPLICATE KEY UPDATE`
- `user_detection_question`：`INSERT ... ON DUPLICATE KEY UPDATE`（基于 `(type_code, question_code)`）

## 6) 结果交付格式

最终交付按以下顺序输出：

1. `SET NAMES utf8mb4;`
2. （若有正文图片）若干条 `arelore_common.common_binary_file` 的 `INSERT ... ON DUPLICATE KEY UPDATE`
3. `USE arelore_education;`（若需指定库）
4. 1 条 `user_detection_type` SQL
5. N 条 `user_detection_question` SQL
6. 题目统计信息：
    - 总题数
    - 含标准答案题数（有 `correctOptionKey`）
    - 含解析题数（有 `explanation` 或 `fullExplanation`）
    - 选项完整率、缺失分值题数（若适用）

若存在不确定映射，在末尾追加「待人工确认清单」。

## 7) 仓库内参考脚本（系统架构设计师）

已提供从「答案详解」类 docx 批量生成 SQL 的脚本（可按年独立输出文件）：

- `arelore-server-admin/src/main/resources/skills/ruankao-exam-to-detection-sql/generate_sysarch_sql.py`

生成结果默认写入：

- `arelore-server-admin/src/main/resources/sql/arelore_education/软考/系统架构设计师/软考_系统架构设计师_*_综合知识_导入.sql`

执行：

```bash
python3 arelore-server-admin/src/main/resources/skills/ruankao-exam-to-detection-sql/generate_sysarch_sql.py
```

（在仓库根目录执行，路径按实际调整。）
