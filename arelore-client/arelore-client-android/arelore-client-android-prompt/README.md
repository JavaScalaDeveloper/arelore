# arelore-client-android-prompt

AI 提示词手机端：登录 Token + 列表查询 + 一键复制提示词/全部字段。

## 配置

App 内「设置」填写：
- API Base，如 `http://10.0.2.2:8081/api`（模拟器访问本机）
- Bearer Token（与 Web 登录后 localStorage `token` 相同）

## 构建

```bash
./gradlew :app:assembleDebug
```

产物：`app/build/outputs/apk/debug/arelore-android-prompt-1.0.0-debug.apk`
