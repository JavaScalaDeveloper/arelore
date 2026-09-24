# Arelore Android Base

Kotlin 实现的 Android 壳工程：通过 WebView 竖屏加载远程 H5（默认 `http://www.arelore.com/home`）。业务内容走远程页面，**内容变更无需重新安装 APK**。

| 项 | 值 |
|------|------|
| 包名 | `com.arelore.android.base` |
| 语言 | Kotlin |
| minSdk / targetSdk | 24 / 35 |
| 默认内容地址 | `http://www.arelore.com/home` |
| 模块路径 | `arelore-client/arelore-client-android/arelore-client-android-base` |

---

## 项目介绍

### 做什么

- 打开 App 后固定**竖屏**展示官网首页移动布局
- 使用 **Native Shell + 远程 H5** 做内容热更新：改网站或远程配置即可，不必发新版 APK
- 可选拉取 `app-config.json`，动态切换内容 URL / 强制清缓存
- 加载失败可重试；系统返回键支持 WebView 历史回退

### 热更新分层

| 层级 | 职责 | 更新方式 |
|------|------|----------|
| APK 壳 | WebView、竖屏、网络、热更新引导 | 应用商店发版 |
| H5 内容 | 首页、产品、文案等业务页面 | 部署网站即可 |
| 远程配置（可选） | `contentUrl` / `forceRefresh` | 发布 JSON |

可选配置（`http://www.arelore.com/app-config.json`）：

```json
{
  "contentUrl": "http://www.arelore.com/home",
  "version": "20260323.1",
  "forceRefresh": true
}
```

配置不存在或拉取失败时，回退到内置地址 `http://www.arelore.com/home`。

### 工程结构

```
arelore-client-android-base/
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/arelore/android/base/
│       │   ├── AreloreApp.kt
│       │   ├── hotupdate/
│       │   │   ├── HotUpdateConfig.kt
│       │   │   └── HotUpdateManager.kt
│       │   └── ui/
│       │       └── MainActivity.kt
│       └── res/
├── gradle/
├── gradlew / gradlew.bat
├── settings.gradle.kts
└── README.md
```

---

## 环境准备

- **Android Studio**：Hedgehog / Ladybug 或更新版本
- **JDK**：17（推荐用 Android Studio 自带 JBR）
- **Android SDK**：API 35；Build-Tools 随 Gradle 自动安装亦可
- **设备**：Android 7.0+ 模拟器或真机，需能访问外网（加载 `www.arelore.com`）

首次用命令行构建时，在本模块根目录确认已有 `local.properties`（Android Studio 打开工程后一般会自动生成）：

```properties
sdk.dir=C\:\\Users\\你的用户名\\AppData\\Local\\Android\\Sdk
```

> `local.properties` 含本机路径，不要提交到 Git。

---

## 怎么启动项目

### 方式一：Android Studio（推荐）

1. 打开 Android Studio → **File → Open**
2. 选择本目录：  
   `arelore-client/arelore-client-android/arelore-client-android-base`
3. 等待右下角 **Gradle Sync** 完成
4. 顶部设备列表选择模拟器或已开启 USB 调试的真机
5. 点击绿色 **Run**（或快捷键 `Shift + F10` / macOS `Control + R`）
6. App 启动后应竖屏打开并加载 `http://www.arelore.com/home`

若 Sync 失败：检查 JDK 17、网络（需拉 Google/Maven 依赖）、`sdk.dir` 是否正确。

### 方式二：命令行安装 Debug 包后启动

在模块根目录执行（Windows 用 `gradlew.bat`，macOS/Linux 用 `./gradlew`）：

```bash
# 编译并安装到已连接设备
gradlew.bat installDebug

# 或先打出 APK 再手动安装
gradlew.bat assembleDebug
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

启动应用：

```bash
adb shell am start -n com.arelore.android.base/.ui.MainActivity
```

---

## 怎么打 Debug / Release 包

以下命令均在 **本模块根目录**（含 `gradlew.bat` 的目录）执行。

### Debug 包

用于日常开发与联调，使用默认 debug 签名，可直接安装。

```bash
# Windows
gradlew.bat assembleDebug

# macOS / Linux
./gradlew assembleDebug
```

| 项 | 说明 |
|------|------|
| 产物路径 | `app/build/outputs/apk/debug/app-debug.apk` |
| 签名 | Android 默认 debug keystore |
| 安装 | `adb install -r app/build/outputs/apk/debug/app-debug.apk` |

一键编译并安装：

```bash
gradlew.bat installDebug
```

### Release 包

用于内测分发或上架前验证。当前工程默认**未开启混淆**（`isMinifyEnabled = false`）。

```bash
# Windows
gradlew.bat assembleRelease

# macOS / Linux
./gradlew assembleRelease
```

| 项 | 说明 |
|------|------|
| 产物路径 | `app/build/outputs/apk/release/app-release-unsigned.apk` |
| 说明 | 未配置正式签名时，产物为**未签名** APK，无法直接安装到部分设备 |

#### 配置正式签名（可选）

1. 生成 keystore（仅首次）：

```bash
keytool -genkey -v -keystore arelore-release.jks -keyalg RSA -keysize 2048 -validity 10000 -alias arelore
```

2. 在 `app/build.gradle.kts` 的 `android { }` 中增加（示例，勿把密码写进仓库）：

```kotlin
signingConfigs {
    create("release") {
        storeFile = file("你的路径/arelore-release.jks")
        storePassword = System.getenv("ARELORE_STORE_PASSWORD")
        keyAlias = "arelore"
        keyPassword = System.getenv("ARELORE_KEY_PASSWORD")
    }
}

buildTypes {
    release {
        isMinifyEnabled = false
        signingConfig = signingConfigs.getByName("release")
        proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro"
        )
    }
}
```

3. 再执行 `assembleRelease`，得到可安装的：

`app/build/outputs/apk/release/app-release.apk`

也可用 Android Studio：**Build → Generate Signed Bundle / APK** 图形化签名打包。

### 常用清理命令

```bash
gradlew.bat clean
gradlew.bat clean assembleDebug
```

### Debug 与 Release 对比

| | Debug | Release |
|--|-------|---------|
| 用途 | 开发调试 | 测试 / 发布 |
| 构建命令 | `assembleDebug` | `assembleRelease` |
| 默认产物 | `app-debug.apk` | `app-release-unsigned.apk`（未配签名时） |
| 签名 | 自动 debug 签名 | 需自行配置正式签名 |
| 安装便利性 | 可直接 `installDebug` | 签名后才能广泛安装 |

---

## 常见问题

**Q: 白屏或一直「正在加载」？**  
检查设备网络是否能打开 `http://www.arelore.com/home`；真机与电脑需都能访问该站点。

**Q: cleartext / 明文 HTTP 报错？**  
工程已配置 `network_security_config` 允许 `arelore.com` 明文流量；若仍失败，确认未被系统或代理拦截 HTTP。

**Q: `gradlew` 找不到或权限错误？**  
Windows 使用 `gradlew.bat`；macOS/Linux 先执行 `chmod +x gradlew`。

**Q: 改了官网内容 App 没变化？**  
热更新依赖远程 H5。可清 WebView 缓存后重进，或在 `app-config.json` 中设 `"forceRefresh": true`。
