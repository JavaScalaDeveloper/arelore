# Android 原生应用

## 子模块

| 模块 | 说明 |
|------|------|
| [arelore-client-android-base](./arelore-client-android-base) | Kotlin 热更新壳：竖屏 WebView 打开 `http://www.arelore.com/home` |
| [arelore-client-android-tax](./arelore-client-android-tax) | 个人所得税 App 模拟器（Compose）：首页 / 收入纳税明细 / 我的 / 收入配置 |
| [arelore-client-android-wechat](./arelore-client-android-wechat) | 微信模拟器占位（可打 APK，后续补会话与聊天） |

## 规划技术栈

- **语言**: Kotlin
- **开发工具**: Android Studio
- **最低版本**: Android 7.0 (API 24)

## 快速开始

分别用 Android Studio 打开对应子目录，或命令行打包：

```bash
# 个税模拟器
cd arelore-client-android-tax && ./gradlew assembleDebug

# 微信模拟器（占位）
cd ../arelore-client-android-wechat && ./gradlew assembleDebug
```

Debug APK 输出路径（含版本号）：

- `arelore-client-android-tax/app/build/outputs/apk/debug/arelore-android-tax-1.0.0-debug.apk`
- `arelore-client-android-wechat/app/build/outputs/apk/debug/arelore-android-wechat-1.0.0-debug.apk`

### 个税模拟器使用说明

1. **首页** → 点「收入纳税明细」→ 选年度/所得类型 →「查询」查看明细
2. **我的** →「收入配置（模拟）」按月配置工资，明细页按累计预扣规则重算税额

## arelore-client-android-prompt
AI 提示词多端同步（Compose），见子目录 README。

