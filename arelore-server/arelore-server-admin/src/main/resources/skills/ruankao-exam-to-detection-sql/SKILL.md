---
name: ruankao-exam-to-detection-sql
description: Parse software-exam question Word documents (doc/docx), remove header/footer noise, and generate MySQL INSERT SQL for user_detection_type and user_detection_question tables. Supports optional standard answer, explanation, and scoring metadata when the source document provides them. Use when user asks to convert exam papers into detection SQL.
---

# Ruankao Exam To Detection SQL

## 适用场景

- 用户给出软考/题库 **Word**（`doc`/`docx`）并要求转成检测题 SQL。**本技能不处理 PDF**；若仅有 PDF，请先用 Word/LibreOffice 或专业工具转为含可复制正文的 `docx`，并整理成下文版式后再跑脚本。
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

### SQL 文件在仓库里的位置（生成后才存在）

技能目录下的脚本**在本地成功跑通后**才会写出 `.sql`；**未执行脚本或解析 0 题时，目标目录里不会有文件**（这是正常现象）。

产出路径由 **`generate_generic_docx_sql.py` 的 `--out-dir`**（或环境变量 **`RUANKAO_OUT_DIR`**）决定，例如：

- `…/sql/arelore_education/软考/<你指定的子目录>/软考_<资格名>_*_导入.sql`

仓库内也可保留「输入目录 → 输出目录」均写死在脚本里的 **示例生成器**（见 §7），仅便于本仓库目录布局复用；**新科目优先使用通用脚本**。

在数据库侧：对生成的 `.sql` 执行 `mysql … < 某文件.sql`（或客户端导入）即可写入 `user_detection_type` / `user_detection_question`（若含图则还有 `arelore_common.common_binary_file`）。

## 支持的格式与脚本（仅 Word）

下表约定如何操作；抽题规则统一由 `ruankao_doc_parse.py` 的 `extract_questions` + `emit_sql` 完成（支持多种版式，见 §3；**含无选项的主观/案例分析题**）。

| 格式 | 说明 | 推荐操作 | 脚本 |
|------|------|----------|------|
| **`.docx`** | Word 2007+，须含**可复制正文**（非整页图片） | `docx_story_text_with_images` → `extract_questions` → `emit_sql` | **`generate_generic_docx_sql.py`**（任意科目，§8）；`generate_sysarch_sql.py`（§7，本仓库目录写死的示例） |
| **`.doc`** | 老版二进制 Word | macOS 下 `generate_generic_docx_sql.py` 会调用 **`textutil`** 转为临时 docx 再解析；其他系统请先另存为 `.docx` | 同上 |

## 执行流程

复制并执行此检查清单：

```text
- [ ] 读取原始文件文本
- [ ] 若有 docx 正文图片：解析关系与 media，计算 SHA-256，生成 common_binary_file SQL，并在题干/解析中写入 【img:…】 与 extra_info.imageHashValues
- [ ] 清理页眉页脚与页码噪音
- [ ] 抽取题目；有选项则抽取 A/B/C/D；无选项的主观/案例题仍入库（`options` 为 `[]`）
- [ ] 若有则抽取标准答案与解析
- [ ] 若有客观题标准答案：为选项配置分值并写入检测类型 `extra_info.scoring`（见下文「算分逻辑」）
- [ ] 生成 detection_type 的 INSERT
- [ ] 生成 detection_question 的 INSERT
- [ ] 校验 question_code 唯一、排序连续
- [ ] 输出可直接执行 SQL（UTF-8）
```

## 1) 文件读取

- **`docx`（Word）**：优先从 `word/document.xml` 按段落顺序抽文本（与 Word 展示一致）；内嵌图经 `a:blip` / `r:embed` 解析为二进制，SHA-256 写入 `common_binary_file`，正文用 `【img:小写hex】` 占位（见 §3.2）。实现入口：`docx_story_text_with_images`（`ruankao_doc_parse.py`）。
- **`doc`（老 Word）**：脚本不直接解析；须 **先转换为 `docx`**（或导出 UTF-8 纯文本并人工整理成与 docx 相同的 `(n)A.` + `【答案】` + `【解析】` 结构），再走 docx 管线。

**公共解析模块**：`ruankao_doc_parse.py` 中的 `extract_questions`、`emit_sql`、`docx_story_text_with_images` 供各 **docx** 生成脚本共用；版式规则变更时只改此处。

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

### 2.2 非官方题库标识（`type_name`）

对 **非官方软考真题**（网络精简卷、题库 docx 等，非历年官方完整试卷），`user_detection_type.type_name` **末尾必须追加** `（非官方）`，例如：

- `软考-多媒体应用设计师-2026年下半年-综合知识（非官方）`

判定由 `ruankao_doc_parse.infer_unofficial_exam` 自动完成（`generate_generic_docx_sql.py` 生成时调用），典型命中条件包括：

| 条件 | 说明 |
|------|------|
| 文件名 | 含「最新」「试题与答案」「真题试题」「考试试题」「年全国软考」等 |
| 正文版式 | Markdown 标题 `### 一、…`、试卷头「考试时间：」、卷面「每题2分，共20分」等 |
| 题量 | 上午/综合类（`JC`/`JJ`/`GK`）客观题 **少于 60 题** |
| 下午误标 | `YY` 卷但全部为客观小题且题数 10–49（实为题库模拟卷） |

命中时 `extra_info` 另写 `"unofficial": true`；`type_description` **不**追加该后缀（仅展示用短标题 `type_name` 需要区分）。

## 3) 题目结构抽取

每题至少包含：

- `question_order`：题号顺序（1..N）
- `question_name`：题干（`varchar(255)`；超长时截断，完整内容写入 `extra_info.fullQuestionName`）
- `question_description`：题型说明（有选项的选择题固定 `单选题`；无选项题为 `案例分析` / `主观题` 等，与 `extra_info.questionType` 一致）
- `options`：JSON 数组（A/B/C/D…）；**无选项题**为 `[]`

### 3.0 支持的 Word 版式（`extract_questions` 按序尝试）

| 版式 | 识别特征 | 典型科目 |
|------|----------|----------|
| **【答案】+【解析】** | `(n)A.` 单行选项 + `【答案】` + `【解析】` | 网络工程师答案详解 |
| **文末答案区** | 正文 `N、` + `A．` 选项；文末 `答案:` + `N、X` + `[解析]` | 数据库系统工程师上午 |
| **试题+行内答案** | `试题N` + 选项 + `答案：X` + 解析 | 电子商务设计师上午 |
| **解答行** | `01.` + 选项 + `解答：答案正确X` | 软件评测师整理版 |
| **N、+答案：** | `N、` 题干 + `答案：X`（无【】） | 网络管理员等 |
| **下午案例（无选项）** | `试题一（共…分）` 大题块，无 A/B/C/D | 下午案例分析 |

全角括号 `（n）` 会在解析前规范为 `(n)`。

### 3.0.1 无选项题（主观 / 案例分析）

- `options` 输出 **`[]`**，不写选项 `scores`。
- `extra_info.questionType` 使用 **`案例分析`**（或文档明确的 `主观题` / `简答题`）。
- **不要**写 `correctOptionKey`（小程序按 `questionType` 将题目标为「主观」，交卷不参与客观判分）。
- 若有参考答案正文，可写入 `extra_info.referenceAnswer`（可选）。
- 检测类型 `extra_info.scoring`：若整卷均无客观题，使用 `resultMode: subjective_review`、`maxScore: 0`；若客观+主观混合，仅对**有选项且含标准答案**的题配置 `objective_sum`，`maxScore` 为客观题数量。

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

**刷题用 SQL 约定（默认）**：

- 题干字段（`question_name` / `fullQuestionName`）**不得包含**标准答案、`[解析]`、`【解析】`、参考答案区正文。
- `extra_info` **不写** `explanation` / `fullExplanation`（解析由用户交卷后另行配置，或后续单独导入）。
- 客观题可保留 `correctOptionKey` 供服务端判分（小程序做题页不展示）；案例分析题不写 `correctOptionKey`。

**上午/下午分卷**：文件名含「上午」→ `type_code` 后缀 `JC`；含「下午」→ `YY`，避免同一年上下半年共用一个 `type_code` 导致题库覆盖。

**文档内嵌图片**（与仓库内已有 `common_binary_file` + `【img:…】` 题库导入脚本一致）：

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
('TYPE_CODE', '检测名称（非官方卷末尾加（非官方））', '检测说明', '{"scoring":{"resultMode":"objective_sum","scoreDimension":"score","maxScore":75,"scoringVersion":"1.0"},"exam":"...","questionCount":75,"unofficial":true}')
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

**有选项（单选）**示例：

```sql
INSERT INTO arelore_education.user_detection_question
(type_code, question_code, question_name, question_order, question_description, options, extra_info)
VALUES
('TYPE_CODE', 'TYPE_CODE_Q001', '题干…', 1, '单选题',
 '[{"key":"A","text":"…","scores":{"score":0}},{"key":"B","text":"…","scores":{"score":1}}]',
 '{"questionType":"单选题","sourceQuestionNo":1,"correctOptionKey":"B","explanation":"……"}')
```

**无选项（案例分析）**示例：

```sql
INSERT INTO arelore_education.user_detection_question
(type_code, question_code, question_name, question_order, question_description, options, extra_info)
VALUES
('TYPE_CODE', 'TYPE_CODE_Q001', '试题一（共15分）…', 1, '案例分析',
 '[]',
 '{"questionType":"案例分析","sourceQuestionNo":1,"explanation":"","imageHashValues":[]}')
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

## 7) 仓库内脚本与分工

- **`ruankao_doc_parse.py`**：`docx_story_text_with_images`、`extract_questions`、`emit_sql`；所有生成脚本共用，版式规则变更时只改此文件。
- **`generate_generic_docx_sql.py`**（**推荐**）：通过命令行参数或环境变量指定 **任意科目** 的 `type_code` 前缀、资格名称、输入根目录、输出目录等，递归处理 `docx` 并写 SQL。详见 **§8**。
- **`generate_sysarch_sql.py`**（**可选示例**）：演示「从本仓库固定相对路径读取 docx、写入固定相对路径 SQL」的写法；输入目录、文件名 glob、输出目录均硬编码在脚本内，**不绑定其它考试**——新资格请优先使用 `generate_generic_docx_sql.py`，或复制该示例脚本并按需改路径与元数据字段。

依赖：仅 **Python 3 标准库** + 同目录 `ruankao_doc_parse.py`，无需 `pip install` 第三方包。

### 7.1 运行示例脚本 `generate_sysarch_sql.py`

在 monorepo 根目录（路径以本仓库为准）：

```bash
python3 arelore-server/arelore-server-admin/src/main/resources/skills/ruankao-exam-to-detection-sql/generate_sysarch_sql.py
```

脚本内的输入/输出路径与资格元数据需自行与仓库布局对齐；若目录或命名不匹配，可能无输出。

## 8) `generate_generic_docx_sql.py`（通用 docx → SQL）

### 8.1 行为摘要

- 从 **`--root`** 递归扫描（需加 **`--recursive`**，或设置环境变量 `RUANKAO_RECURSIVE=1`）收集 `*.docx`；跳过非 zip 的损坏文件。
- 从文件名解析 **年份**（`19xx` / `20xx`）、**上/下半年**、可选 **`（第N批）`**。
- **`type_code`**：`{type_prefix}_{YYYY}{H1|H2}[_B{n}]_后缀`。后缀规则：文件名含 **答案详解 / 答案解析** → `JJ`；**基础知识** → `JC`；**应用技术** → `YY`；否则使用 **`--default-session-suffix`**（默认 `GK`，适合文件名不含「答案」的客观卷）。
- **`--name-substr`**：仅处理文件名包含该子串的 docx（例如只处理含「答案」的解析类文档）。
- 输出文件名：`软考_{--exam-name}_{年}{上|下}半年_{原文件名摘要}_导入.sql`，写入 **`--out-dir`**。
- 若直接传入若干 **`.docx` 路径**（positional），则只处理这些文件，**忽略 `--root`**。

### 8.2 参数与环境变量

| 含义 | CLI | 环境变量 |
|------|-----|----------|
| type_code 前缀 | `--type-prefix` | `RUANKAO_TYPE_PREFIX` |
| 资格名称（写入 `extra_info.exam` 等） | `--exam-name` | `RUANKAO_EXAM_NAME` |
| 级别说明（可选，写入描述） | `--exam-level` | `RUANKAO_EXAM_LEVEL` |
| 输出目录 | `--out-dir` | `RUANKAO_OUT_DIR` |
| 扫描根目录 | `--root` | `RUANKAO_DOCX_ROOT` |
| 文件名须包含 | `--name-substr` | `RUANKAO_NAME_SUBSTR` |
| 默认 type 后缀 | `--default-session-suffix` | `RUANKAO_DEFAULT_SESSION_SUFFIX` |
| 仅处理前 N 个文件 | （仅 env） | `RUANKAO_MAX_DOCX` |

### 8.3 调用示例

**程序员**（历年真题目录下仅处理文件名含「答案」的 docx）：

```bash
python3 arelore-server/arelore-server-admin/src/main/resources/skills/ruankao-exam-to-detection-sql/generate_generic_docx_sql.py \
  --type-prefix RK_PROG \
  --exam-name 程序员 \
  --exam-level 初级 \
  --out-dir arelore-server/arelore-server-admin/src/main/resources/sql/arelore_education/软考/初级/程序员 \
  --root arelore-server/arelore-server-admin/src/main/resources/sql/arelore_education/软考/初级/程序员历年真题 \
  --recursive \
  --name-substr 答案
```

**信息系统项目管理师**（选择题 docx 文件名通常无「答案」字样，`type_code` 后缀用 `GK`）：

```bash
python3 arelore-server/arelore-server-admin/src/main/resources/skills/ruankao-exam-to-detection-sql/generate_generic_docx_sql.py \
  --type-prefix RK_IPMP \
  --exam-name 信息系统项目管理师 \
  --out-dir arelore-server/arelore-server-admin/src/main/resources/sql/arelore_education/软考/信息系统项目管理师 \
  --root arelore-server/arelore-server-admin/src/main/resources/sql/arelore_education/软考/2015-2024年高级信息系统项目管理师历年真题合集/2015-2024年选择题真题 \
  --recursive \
  --default-session-suffix GK
```

### 8.4 常见问题

- **0 题**：正文不符合 §3.0 任一款式，或 docx 为整页扫描图（无可复制文字）。
- **下午卷题数少**：按 `试题一（共…分）` 分块，一题一大案例属正常；上午选择题应接近 75 题量级。
- **SKIP 无法解析年份/场次**：文件名中需含四位年份与「上/下半年」等可识别片段。
