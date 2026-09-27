import Foundation

/// Compile-time defaults, equivalent to Android `BuildConfig`.
enum AppConfig {
    static let defaultContentURL = "http://www.arelore.com/home"
    static let hotUpdateConfigURL = "http://www.arelore.com/app-config.json"
    static let versionName = "1.0.0"
}
