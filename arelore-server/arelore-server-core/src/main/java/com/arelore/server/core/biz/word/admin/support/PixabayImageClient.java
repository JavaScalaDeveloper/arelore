package com.arelore.server.core.biz.word.admin.support;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Pixabay 免费图库：无水印、可免费使用（需遵守 Pixabay 许可）。
 * 文档：https://pixabay.com/api/docs/
 */
@Component
public class PixabayImageClient {
    private static final String SEARCH_URL = "https://pixabay.com/api/";
    private static final HttpClient CLIENT = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(8))
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build();

    @Value("${arelore.word.image.pixabay-api-key:}")
    private String apiKey;

    @Value("${arelore.word.image.min-count:4}")
    private int minCount;

    @Value("${arelore.word.image.search-count:12}")
    private int searchCount;

    public boolean configured() {
        return StringUtils.hasText(apiKey);
    }

    public int minCount() {
        return Math.max(4, minCount);
    }

    public List<Map<String, String>> search(String query) throws Exception {
        return search(query, minCount());
    }

    public List<Map<String, String>> search(String query, int minRequired) throws Exception {
        if (!configured()) {
            throw new IllegalStateException("未配置 arelore.word.image.pixabay-api-key（可用环境变量 ARELORE_PIXABAY_API_KEY）");
        }
        if (!StringUtils.hasText(query)) {
            throw new IllegalArgumentException("搜索词为空");
        }
        int need = Math.max(1, minRequired);
        int perPage = Math.max(need, Math.min(50, Math.max(searchCount, need)));
        String q = URLEncoder.encode(query.trim(), StandardCharsets.UTF_8);
        String key = URLEncoder.encode(apiKey.trim(), StandardCharsets.UTF_8);
        URI uri = URI.create(SEARCH_URL + "?key=" + key
            + "&q=" + q
            + "&image_type=photo"
            + "&lang=en"
            + "&safesearch=true"
            + "&per_page=" + perPage);
        HttpRequest request = HttpRequest.newBuilder()
            .uri(uri)
            .timeout(Duration.ofSeconds(20))
            .header("User-Agent", "arelore-admin/1.0")
            .GET()
            .build();
        HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() == 401 || response.statusCode() == 403) {
            throw new IllegalStateException("Pixabay API Key 无效或无权限");
        }
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("Pixabay HTTP " + response.statusCode());
        }
        JSONObject root = JSON.parseObject(response.body());
        if (root == null) {
            throw new IllegalStateException("Pixabay 响应非 JSON");
        }
        JSONArray hits = root.getJSONArray("hits");
        List<Map<String, String>> list = new ArrayList<>();
        if (hits != null) {
            for (int i = 0; i < hits.size(); i++) {
                JSONObject hit = hits.getJSONObject(i);
                if (hit == null) {
                    continue;
                }
                String url = firstNonBlank(
                    hit.getString("largeImageURL"),
                    hit.getString("webformatURL"),
                    hit.getString("previewURL")
                );
                String thumb = firstNonBlank(
                    hit.getString("previewURL"),
                    hit.getString("webformatURL"),
                    url
                );
                if (!StringUtils.hasText(url)) {
                    continue;
                }
                Map<String, String> item = new LinkedHashMap<>();
                item.put("url", url.trim());
                item.put("thumbUrl", StringUtils.hasText(thumb) ? thumb.trim() : url.trim());
                item.put("source", "PIXABAY");
                if (StringUtils.hasText(hit.getString("user"))) {
                    item.put("photographer", hit.getString("user").trim());
                }
                if (StringUtils.hasText(hit.getString("pageURL"))) {
                    item.put("pageUrl", hit.getString("pageURL").trim());
                }
                list.add(item);
            }
        }
        if (list.size() < need) {
            throw new IllegalStateException("图库结果不足 " + need + " 张（当前 " + list.size() + "），可换关键词重试");
        }
        return list;
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return "";
        }
        for (String v : values) {
            if (StringUtils.hasText(v)) {
                return v;
            }
        }
        return "";
    }
}
