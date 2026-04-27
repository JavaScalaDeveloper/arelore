---
name: ruankao-exam-to-detection-sql
description: Parse software-exam question files (doc/docx/pdf), remove header/footer noise, and generate MySQL INSERT SQL for user_detection_type and user_detection_question tables. Use when user asks to convert exam papers into detection SQL.
---

# Ruankao Exam To Detection SQL

## 适用场景

- 用户给出软考/题库文件（`doc`/`docx`/`pdf`）并要求转成检测题 SQL。
- 目标表为：
    - `arelore_education.user_detection_type`
    - `arelore_education.user_detection_question`

## 输出目标

1. 生成 1 条 `user_detection_type` 的 `INSERT` 语句。
2. 生成 N 条 `user_detection_question` 的 `INSERT` 语句（每题一条）。
3. `user_detection_question.type_code` 必须等于检测项目 `type_code`。
4. 满足唯一约束：
    - `user_detection_type`: `type_code`
    - `user_detection_question`: `(type_code, question_code)`

## 执行流程

复制并执行此检查清单：

```text
- [ ] 读取原始文件文本
- [ ] 清理页眉页脚与页码噪音
- [ ] 抽取题目、选项、分值/维度映射
- [ ] 生成 detection_type 的 INSERT
- [ ] 生成 detection_question 的 INSERT
- [ ] 校验 question_code 唯一、排序连续
- [ ] 输出可直接执行 SQL（UTF-8）
```

## 1) 文件读取

- `pdf`：优先直接读取文本；若有乱码/结构错乱，再逐页抽取并重排。
- `docx`：读取段落 + 编号列表。
- `doc`：先转换为 `docx`/纯文本再解析（保持原文顺序）。

## 2) 页眉页脚清理规则

忽略重复出现且不属于题干/选项的行，例如：

- 固定标题（每页重复）
- 页码行（如 `第 X 页`、`Page X`、`- 12 -`）
- 时间/版权水印行

建议规则：

- 若某行在全文出现频率异常高且长度短（如 < 30 字），优先判定为页眉页脚噪音。
- 仅删除“重复噪音行”，不要删除题干中的重复术语。

## 3) 题目结构抽取

每题至少包含：

- `question_order`：题号顺序（1..N）
- `question_name`：题干（简短标题）
- `question_description`：默认 `请选择最符合你的一项`（无特殊说明时）
- `options`：JSON 数组（A/B/C/D...）

`options` JSON 元素格式：

```json
{
  "key": "A",
  "text": "选项文本",
  "scores": { "R": 1 },
  "dimension": "R"
}
```

如果原题未明确维度/分值：

- 将 `scores` 置为 `{}`，
- `dimension` 置为 `null`，
- 并在 `extra_info` 标注 `"scorePending": true`。

## 4) SQL 生成规范

### 4.1 检测项目（1 条）

使用如下结构：

```sql
INSERT INTO arelore_education.user_detection_type
(type_code, type_name, type_description, extra_info)
VALUES
('TYPE_CODE', '检测名称', '检测说明', '{"scoring": {...}, "resultMeanings": {...}}');
```

### 4.2 题目（N 条）

使用如下结构：

```sql
INSERT INTO arelore_education.user_detection_question
(type_code, question_code, question_name, question_order, question_description, options, extra_info)
VALUES
('TYPE_CODE', 'CODE001', '题干', 1, '请选择最符合你的一项', '[{"key":"A","text":"...","scores":{"R":1},"dimension":"R"}]', null);
```

`question_code` 规则建议：

- 前缀取 `type_code` 前 3~4 位语义缩写，后接 3 位序号，例如 `RKE001`。
- 保证同一 `type_code` 内唯一。

## 5) 唯一索引冲突处理（必须实现）

输出 SQL 时优先使用幂等写法，避免重复导入失败：

- `user_detection_type`：
    - `INSERT ... ON DUPLICATE KEY UPDATE`
- `user_detection_question`：
    - `INSERT ... ON DUPLICATE KEY UPDATE`
    - 基于唯一键 `(type_code, question_code)` 更新题干、顺序、选项等字段。

示例：

```sql
INSERT INTO arelore_education.user_detection_question
(type_code, question_code, question_name, question_order, question_description, options, extra_info)
VALUES
('TYPE_CODE', 'RKE001', '示例题', 1, '请选择最符合你的一项', '[{"key":"A","text":"...","scores":{"R":1},"dimension":"R"}]', null)
ON DUPLICATE KEY UPDATE
question_name = VALUES(question_name),
question_order = VALUES(question_order),
question_description = VALUES(question_description),
options = VALUES(options),
extra_info = VALUES(extra_info);
```

## 6) 结果交付格式

最终交付按以下顺序输出：

1. `SET NAMES utf8mb4;`
2. 1 条 `user_detection_type` SQL
3. N 条 `user_detection_question` SQL
4. 题目统计信息（总题数、选项完整率、缺失分值题数）

若存在不确定映射（例如维度缺失），在末尾追加“待人工确认清单”。
