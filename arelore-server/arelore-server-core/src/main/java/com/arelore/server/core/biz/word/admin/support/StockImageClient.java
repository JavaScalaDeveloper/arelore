package com.arelore.server.core.biz.word.admin.support;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 图库搜图统一入口：优先 Unsplash（可新申请 Key），其次 Pexels（若已有旧 Key）。
 */
@Component
public class StockImageClient {
    private final UnsplashImageClient unsplashImageClient;
    private final PexelsImageClient pexelsImageClient;

    public StockImageClient(UnsplashImageClient unsplashImageClient, PexelsImageClient pexelsImageClient) {
        this.unsplashImageClient = unsplashImageClient;
        this.pexelsImageClient = pexelsImageClient;
    }

    public int minCount() {
        if (unsplashImageClient.configured()) {
            return unsplashImageClient.minCount();
        }
        return pexelsImageClient.minCount();
    }

    public String activeSource() {
        if (unsplashImageClient.configured()) {
            return "UNSPLASH";
        }
        if (pexelsImageClient.configured()) {
            return "PEXELS";
        }
        return "";
    }

    public List<Map<String, String>> search(String query) throws Exception {
        if (unsplashImageClient.configured()) {
            return unsplashImageClient.search(query);
        }
        if (pexelsImageClient.configured()) {
            return pexelsImageClient.search(query);
        }
        throw new IllegalStateException(
            "未配置图库 Key。请申请 Unsplash Access Key 并配置 arelore.word.image.unsplash-access-key"
                + "（环境变量 ARELORE_UNSPLASH_ACCESS_KEY）。申请：https://unsplash.com/developers"
                + "；若已有 Pexels Key 可配置 arelore.word.image.pexels-api-key"
        );
    }
}
