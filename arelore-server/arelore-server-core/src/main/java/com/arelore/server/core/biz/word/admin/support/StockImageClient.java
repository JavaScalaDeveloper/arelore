package com.arelore.server.core.biz.word.admin.support;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/** 图库搜图：仅 Pixabay */
@Component
public class StockImageClient {
    private final PixabayImageClient pixabayImageClient;

    public StockImageClient(PixabayImageClient pixabayImageClient) {
        this.pixabayImageClient = pixabayImageClient;
    }

    public int minCount() {
        return pixabayImageClient.minCount();
    }

    public String activeSource() {
        return pixabayImageClient.configured() ? "PIXABAY" : "";
    }

    public List<Map<String, String>> search(String query) throws Exception {
        return search(query, minCount());
    }

    public List<Map<String, String>> search(String query, int minRequired) throws Exception {
        if (!pixabayImageClient.configured()) {
            throw new IllegalStateException(
                "未配置 arelore.word.image.pixabay-api-key（环境变量 ARELORE_PIXABAY_API_KEY）"
            );
        }
        return pixabayImageClient.search(query, minRequired);
    }
}
