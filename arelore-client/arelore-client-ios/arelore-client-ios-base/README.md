# Arelore iOS Base

Swift / UIKit 实现的 iOS 壳工程：通过 WKWebView 竖屏加载远程 H5（默认 `http://www.arelore.com/home`）。业务内容走远程页面，**内容变更无需重新安装 IPA**。

| 项 | 值 |
|------|------|
| Bundle ID | `com.arelore.ios.base` |
| 语言 | Swift 5 |
| 最低系统 | iOS 13.0 |
| 默认内容地址 | `http://www.arelore.com/home` |
| 模块路径 | `arelore-client/arelore-client-ios/arelore-client-ios-base` |

---

## 项目介绍

### 做什么

- 打开 App 后固定**竖屏**展示官网首页移动布局
- 使用 **Native Shell + 远程 H5** 做内容热更新：改网站或远程配置即可，不必发新版 IPA
- 可选拉取 `app-config.json`，动态切换内容 URL / 强制清缓存
- 加载失败可重试；支持 WebView 历史手势回退

### 热更新分层

| 层级 | 职责 | 更新方式 |
|------|------|----------|
| IPA 壳 | WKWebView、竖屏、网络、热更新引导 | App Store / 企业分发 |
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

配置不存在或拉取失败时，优先使用上次缓存的地址，再回退到内置地址 `http://www.arelore.com/home`。

### 工程结构

```
arelore-client-ios-base/
├── Arelore.xcodeproj/
├── Arelore/
│   ├── AppDelegate.swift
│   ├── AppConfig.swift
│   ├── MainViewController.swift
│   ├── HotUpdate/
│   │   ├── HotUpdateConfig.swift
│   │   └── HotUpdateManager.swift
│   ├── Assets.xcassets/
│   ├── LaunchScreen.storyboard
│   ├── Info.plist
│   └── PrivacyInfo.xcprivacy
└── README.md
```

---

## 环境准备

- **Xcode**：15 或更新版本
- **macOS**：能运行对应 Xcode 的版本
- **设备**：iOS 13+ 模拟器或真机，需能访问外网（加载 `www.arelore.com`）
- **真机调试**：在 Xcode 中登录 Apple ID，选择 Development Team

---

## 怎么启动项目

### 方式一：Xcode（推荐）

1. 打开 Xcode → **File → Open**
2. 选择本目录下的工程：  
   `arelore-client/arelore-client-ios/arelore-client-ios-base/Arelore.xcodeproj`
3. 顶部选择模拟器或已连接的真机
4. 真机首次运行时，在 **Signing & Capabilities** 中选择你的 Team
5. 点击 **Run**（快捷键 `Command + R`）
6. App 启动后应竖屏打开并加载 `http://www.arelore.com/home`

### 方式二：命令行编译

在模块根目录执行：

```bash
# 列出可用模拟器
xcrun simctl list devices available

# 编译（模拟器）
xcodebuild -project Arelore.xcodeproj -scheme Arelore -configuration Debug \
  -destination 'platform=iOS Simulator,name=iPhone 16' \
  CODE_SIGNING_ALLOWED=NO build
```

---

## 怎么打 Debug / Release 包

iOS **不能像 Android 那样安装未签名包**。真机可安装的产物是签过名的 `.ipa`（或 Xcode 直接装上的 `.app`）。以下操作均在 **本模块根目录**（含 `Arelore.xcodeproj` 的目录）执行。

打真机包前，先在 Xcode 打开工程：

1. 选中 Target **Arelore** → **Signing & Capabilities**
2. 勾选 **Automatically manage signing**
3. **Team** 选你的 Apple ID / 开发者账号（免费账号也可装自己的真机，有效期约 7 天）

把 `YOUR_TEAM_ID` 换成账号的 Team ID（Xcode → Settings → Accounts → 选中账号 → Team ID，10 位字母数字）。

---

### Debug 包

用于日常开发与联调。可调试、符号完整，安装范围限于你签名里登记过的设备。

#### 方式 A：Xcode 直接装到模拟器 / 真机（最快，不产出独立 IPA）

1. 顶部 Scheme 选 **Arelore**，配置保持 **Debug**
2. 目的地选模拟器，或已解锁、已信任电脑的真机
3. **Product → Run**（`Command + R`）

这会编译 Debug 并安装启动，一般**不会**在磁盘上留下可转发的 `.ipa`。

#### 方式 B：打出 Debug `.app`（模拟器，无需签名）

```bash
xcodebuild -project Arelore.xcodeproj -scheme Arelore -configuration Debug \
  -destination 'generic/platform=iOS Simulator' \
  -derivedDataPath build/DerivedData \
  CODE_SIGNING_ALLOWED=NO build
```

| 项 | 说明 |
|------|------|
| 产物路径 | `build/DerivedData/Build/Products/Debug-iphonesimulator/Arelore.app` |
| 签名 | 不需要 |
| 安装 | 只能装模拟器，不能装真机 |

安装到已启动的模拟器：

```bash
xcrun simctl install booted build/DerivedData/Build/Products/Debug-iphonesimulator/Arelore.app
xcrun simctl launch booted com.arelore.ios.base
```

#### 方式 C：打出 Debug `.ipa`（真机可安装、可发给同 Team 设备）

**图形界面：**

1. 顶部目的地选 **Any iOS Device (arm64)**，不要选模拟器
2. **Product → Scheme → Edit Scheme… → Archive**，把 **Build Configuration** 改成 **Debug**
3. **Product → Archive**，等 Organizer 出现归档
4. **Distribute App** → **Development** → 按向导导出
5. 得到 `Arelore.ipa`

**命令行：**

先准备 `ExportOptions-Debug.plist`（可放在模块根目录，不要把 Team ID 提交进仓库也行，本地自用即可）：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>method</key>
    <string>development</string>
    <key>signingStyle</key>
    <string>automatic</string>
    <key>teamID</key>
    <string>YOUR_TEAM_ID</string>
    <key>compileBitcode</key>
    <false/>
    <key>stripSwiftSymbols</key>
    <false/>
</dict>
</plist>
```

```bash
# 1. 归档（Debug）
xcodebuild -project Arelore.xcodeproj -scheme Arelore -configuration Debug \
  -destination 'generic/platform=iOS' \
  -archivePath build/Arelore-Debug.xcarchive \
  archive

# 2. 导出 IPA
xcodebuild -exportArchive \
  -archivePath build/Arelore-Debug.xcarchive \
  -exportPath build/ipa-debug \
  -exportOptionsPlist ExportOptions-Debug.plist
```

| 项 | 说明 |
|------|------|
| 产物路径 | `build/ipa-debug/Arelore.ipa` |
| 签名 | Development 证书（自动签名） |
| 安装对象 | 已加入该 Team / 描述文件的真机 |

安装 Debug IPA 到已连接真机（任选其一）：

- Xcode：**Window → Devices and Simulators** → 选设备 → 把 `.ipa` 拖进 **Installed Apps**
- Finder：设备连上后，把 `.ipa` 拖到设备图标上
- 命令行（需已装 Apple 设备支持 / `devicectl`，Xcode 15+）：

```bash
# 先查设备 UDID
xcrun devicectl list devices

xcrun devicectl device install app --device <UDID> build/ipa-debug/Arelore.ipa
```

---

### Release 包

用于内测分发或上架。默认走 Scheme 里 Archive 的 **Release** 配置（优化、去掉调试符号）。

当前工程**未开启 Bitcode**，也没有额外混淆插件。

#### 方式 A：Xcode Archive 后导出 IPA（推荐）

1. 确认 Scheme 的 **Archive** 配置为 **Release**（默认就是）
2. 顶部目的地选 **Any iOS Device (arm64)**
3. **Product → Archive**
4. Organizer 中选中归档 → **Distribute App**，按用途选导出方式：

| 导出方式 | 得到什么 | 谁能装 |
|----------|----------|--------|
| **Debugging / Development** | 开发 IPA | 描述文件里的设备 |
| **Ad Hoc** | 内测 IPA | 最多约 100 台已登记 UDID 的设备（付费账号） |
| **App Store Connect** | 上架包 | 不能直接装真机，需提交 TestFlight / App Store |
| **Enterprise** | 企业包 | 仅 Apple 企业账号 |

向导结束后得到 `.ipa`，保存路径由你在导出时选择。

#### 方式 B：命令行 Archive + 导出

按分发方式准备对应 plist（把 `YOUR_TEAM_ID` 换成真实值）。

内测 Ad Hoc 示例 `ExportOptions-Release-AdHoc.plist`：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>method</key>
    <string>ad-hoc</string>
    <key>signingStyle</key>
    <string>automatic</string>
    <key>teamID</key>
    <string>YOUR_TEAM_ID</string>
    <key>compileBitcode</key>
    <false/>
    <key>stripSwiftSymbols</key>
    <true/>
</dict>
</plist>
```

上架示例 `ExportOptions-Release-AppStore.plist`：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>method</key>
    <string>app-store-connect</string>
    <key>signingStyle</key>
    <string>automatic</string>
    <key>teamID</key>
    <string>YOUR_TEAM_ID</string>
    <key>uploadSymbols</key>
    <true/>
</dict>
</plist>
```

```bash
# 1. 归档（Release）
xcodebuild -project Arelore.xcodeproj -scheme Arelore -configuration Release \
  -destination 'generic/platform=iOS' \
  -archivePath build/Arelore-Release.xcarchive \
  archive

# 2. 导出内测 IPA（Ad Hoc）
xcodebuild -exportArchive \
  -archivePath build/Arelore-Release.xcarchive \
  -exportPath build/ipa-release \
  -exportOptionsPlist ExportOptions-Release-AdHoc.plist
```

| 项 | 说明 |
|------|------|
| 归档路径 | `build/Arelore-Release.xcarchive` |
| IPA 路径 | `build/ipa-release/Arelore.ipa` |
| 签名 | Distribution / Ad Hoc 证书（自动签名） |
| 安装 | Ad Hoc：与 Debug IPA 相同方式装真机；App Store：用 Transporter 或 Organizer 上传 |

未配置 Team 时 `archive` / `exportArchive` 会失败，这是预期行为，不是工程缺文件。

---

### 常用清理命令

```bash
rm -rf build
xcodebuild -project Arelore.xcodeproj -scheme Arelore clean
```

Xcode 图形界面：**Product → Clean Build Folder**（`Shift + Command + K`）。

---

### Debug 与 Release 对比

| | Debug | Release |
|--|-------|---------|
| 用途 | 开发调试 | 内测 / 上架 |
| 构建命令 | `-configuration Debug` | `-configuration Release` + `archive` |
| 默认可安装产物 | 模拟器 `.app`；真机需 Development `.ipa` | Ad Hoc / App Store `.ipa` |
| 产物示例 | `build/ipa-debug/Arelore.ipa` | `build/ipa-release/Arelore.ipa` |
| 签名 | 开发证书 | 发布 / Ad Hoc 证书 |
| 优化与符号 | 未优化，便于断点调试 | 优化编译，可剥离 Swift 符号 |
| 安装便利性 | Xcode Run 最方便；IPA 仅限已登记设备 | 签名并匹配描述文件后才能装；商店包走 TestFlight |

---

## 常见问题

**Q: 白屏或一直「正在加载」？**  
检查设备网络是否能打开 `http://www.arelore.com/home`。

**Q: ATS / 明文 HTTP 被拦截？**  
工程已在 `Info.plist` 为 `arelore.com` 配置 `NSExceptionAllowsInsecureHTTPLoads`。

**Q: 真机报签名错误？**  
打开 Target → Signing & Capabilities，勾选 Automatically manage signing 并选择 Team。

**Q: 改了官网内容 App 没变化？**  
热更新依赖远程 H5。可在 `app-config.json` 中设 `"forceRefresh": true`，或杀掉 App 后重进。
