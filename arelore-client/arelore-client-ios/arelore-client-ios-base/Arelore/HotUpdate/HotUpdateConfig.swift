import Foundation

/// Remote hot-update config payload.
///
/// Backend can publish JSON like:
/// ```
/// {
///   "contentUrl": "http://www.arelore.com/home",
///   "version": "20260323.1",
///   "forceRefresh": true
/// }
/// ```
///
/// Changing this JSON updates in-app content without releasing a new IPA.
struct HotUpdateConfig {
    let contentUrl: String
    let version: String?
    let forceRefresh: Bool
}
