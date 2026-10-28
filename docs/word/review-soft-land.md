# 复习债软着陆

防止断签后「待复习爆炸」劝退：候选池有限 + 每日上限 + 断签压缩。

## 规则

| 配置（`plan.ext_info`） | 默认 | 含义 |
|-------------------------|------|------|
| `reviewLookbackDays` | `3`（1～7） | 只从最近 N 个**有新学的日历日**里抽复习词 |
| `softLandEnabled` | `true` | 断签 ≥2 天且当日尚未开练时，今日复习目标再压到约「每日新学」量（至少 10） |

今日复习目标：

```
raw = min(计划每日复习上限, 回溯池内未今日复习的候选数)
若 softLand 且 gapDays≥2 且当日 0 新学/0 复习：
  target = min(raw, max(10, dailyNew))
否则：
  target = raw
```

`gapDays` = 今天 − 最近一次 `studyDates` / `studyDate` 的日历天数。

## 实现要点

- 抽词：`AdminWordEntryMapper.selectForReview(..., lookbackDays)` + `LIMIT target`
- 计数：`countReviewCandidates`（同条件）
- 会话：`UserWordLearnRecordServiceImpl` 的 `createSession` / `enrichHome`
- 计划页（Web / 小程序）可改回溯天数与软着陆开关；服务端 `UserWordStudyPlanServiceImpl` 归一化写入

## 与「高利贷 SRS」的区别

不做全量 SRS 欠债清算；每日硬封顶 + 断签软压缩，保证回来能学完一轮。
