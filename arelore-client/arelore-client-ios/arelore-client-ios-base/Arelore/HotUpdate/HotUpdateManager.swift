import Foundation

/// Lightweight content hot-update:
/// - Shell IPA only provides WKWebView container + network bootstrap
/// - Business content is remote H5; server-side changes take effect without reinstall
/// - Optional remote JSON can switch `contentUrl` / force cache refresh
final class HotUpdateManager {

    private let defaults: UserDefaults

    init(defaults: UserDefaults = .standard) {
        self.defaults = defaults
    }

    func resolveContentUrl(completion: @escaping (HotUpdateConfig) -> Void) {
        fetchRemoteConfig { [weak self] remote in
            guard let self = self else { return }

            if let remote = remote {
                self.persist(remote)
                DispatchQueue.main.async { completion(remote) }
                return
            }

            if let cachedURL = self.defaults.string(forKey: Keys.contentURL), !cachedURL.isEmpty {
                let cached = HotUpdateConfig(
                    contentUrl: cachedURL,
                    version: self.defaults.string(forKey: Keys.version),
                    forceRefresh: false
                )
                DispatchQueue.main.async { completion(cached) }
                return
            }

            DispatchQueue.main.async {
                completion(HotUpdateConfig(contentUrl: AppConfig.defaultContentURL, version: nil, forceRefresh: false))
            }
        }
    }

    private func fetchRemoteConfig(completion: @escaping (HotUpdateConfig?) -> Void) {
        guard let url = URL(string: AppConfig.hotUpdateConfigURL) else {
            completion(nil)
            return
        }

        var request = URLRequest(url: url)
        request.httpMethod = "GET"
        request.setValue("application/json", forHTTPHeaderField: "Accept")
        request.timeoutInterval = 4

        let task = URLSession.shared.dataTask(with: request) { data, response, error in
            if let error = error {
                NSLog("[HotUpdateManager] Hot-update config unavailable: %@", error.localizedDescription)
                completion(nil)
                return
            }

            guard let http = response as? HTTPURLResponse, (200...299).contains(http.statusCode) else {
                let code = (response as? HTTPURLResponse)?.statusCode ?? -1
                NSLog("[HotUpdateManager] Hot-update config HTTP %d", code)
                completion(nil)
                return
            }

            guard let data = data, let parsed = Self.parseConfig(data) else {
                completion(nil)
                return
            }
            completion(parsed)
        }
        task.resume()
    }

    private static func parseConfig(_ data: Data) -> HotUpdateConfig? {
        guard
            let json = try? JSONSerialization.jsonObject(with: data) as? [String: Any]
        else {
            return nil
        }

        let contentUrl = (json["contentUrl"] as? String).flatMap { $0.isEmpty ? nil : $0 }
            ?? (json["url"] as? String).flatMap { $0.isEmpty ? nil : $0 }
        guard let contentUrl = contentUrl else { return nil }

        let version = (json["version"] as? String).flatMap { $0.isEmpty ? nil : $0 }
        let forceRefresh = json["forceRefresh"] as? Bool ?? false
        return HotUpdateConfig(contentUrl: contentUrl, version: version, forceRefresh: forceRefresh)
    }

    private func persist(_ config: HotUpdateConfig) {
        defaults.set(config.contentUrl, forKey: Keys.contentURL)
        defaults.set(config.version, forKey: Keys.version)
    }

    private enum Keys {
        static let contentURL = "arelore_hot_update_content_url"
        static let version = "arelore_hot_update_version"
    }
}
