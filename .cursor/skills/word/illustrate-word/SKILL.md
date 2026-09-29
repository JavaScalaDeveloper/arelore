---
name: illustrate-word
description: >-
  Generates four distinct illustration images for a vocabulary word. If the word
  has multiple senses, different images cover different meanings. Use when the
  user provides a word and wants pictures, 单词配图, 词条插图, or examples like
  “输入 cat 生成 4 张猫的图片”. Word-app project skill under .cursor/skills/word/.
---

# 单词配图（4 张）

用户给出一个单词时，立刻生成 **4 张互不相同的图片**，用来辅助背单词。不要只写文案、不要只出 1 张。

## 流程

1. 确认目标单词（用户消息里的词；一词多义时在回复里用中文点明你覆盖了哪些意思）。
2. 规划 4 个画面，再调用图片生成工具 **4 次**（一次一张，提示词互不重复）。
3. 每张图：`aspect_ratio` 用 `1:1`；`filename` 用 `{单词小写}-{序号}.png`（如 `cat-1.png`）。
4. 不要把生成结果再以 Markdown 图片贴一遍；客户端会自己展示工具产出的图。
5. 回复用简体中文：单词、4 张各自对应的释义/场景（一两句即可）。

## 怎么拆 4 张

- **单一常用义**（如 cat）：4 张都是该事物，但场景/品种/构图必须明显不同。  
  例：室内橘猫；草地上的狸花猫；黑猫特写；猫和毛线球。
- **多个常见义**：优先给不同意思各出图；凑满 4 张。  
  例 bank：河岸、银行柜台、存钱罐、河岸黄昏——至少覆盖「河岸」和「银行」。
- 画面要 **具体、可画、无文字**（不要在图里写英文单词本身）。风格统一为清晰插画/照片写实择一，4 张保持同一风格。
- 适合教学：主体居中、背景干净、一眼能对上释义。

## 调用约定

使用 Cursor 的 `GenerateImage`（`cursor` 命名空间）。`description` 写成完整英文画面描述（主体、环境、光线、构图），四次描述不得雷同。

用户示例：「cat」→ 生成 4 张不同的猫。
