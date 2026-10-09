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
 * Pexels 免费图库：无水印、可商用（需保留摄影师署名信息于候选元数据）。
 * 文档：https://www.pexels.com/api/documentation/
 */
@Component
public class PexelsImageClient {
    private static final String SEARCH_URL = "https://api.pexels.com/v1/search";
    private static final HttpClient CLIENT = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(8))
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build();

    @Value("${arelore.word.image.pexels-api-key:}")
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

    /**
     * 搜索候选图，不落库。至少返回 minCount 张，否则抛错。
     */
    public List<Map<String, String>> search(String query) throws Exception {
        if (!configured()) {
            throw new IllegalStateException("未配置 arelore.word.image.pexels-api-key（可用环境变量 ARELORE_PEXELS_API_KEY）");
        }
        if (!StringUtils.hasText(query)) {
            throw new IllegalArgumentException("搜索词为空");
        }
        int perPage = Math.max(minCount(), Math.min(80, searchCount));
        String q = URLEncoder.encode(query.trim(), StandardCharsets.UTF_8);
        URI uri = URI.create(SEARCH_URL + "?query=" + q + "&per_page=" + perPage + "&orientation=landscape");
        HttpRequest request = HttpRequest.newBuilder()
            .uri(uri)
            .timeout(Duration.ofSeconds(20))
            .header("Authorization", apiKey.trim())
            .header("User-Agent", "arelore-admin/1.0")
            .GET()
            .build();
        HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() == 401 || response.statusCode() == 403) {
            throw new IllegalStateException("Pexels API Key 无效或无权限");
        }
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("Pexels HTTP " + response.statusCode());
        }
        JSONObject root = JSON.parseObject(response.body());
        if (root == null) {
            throw new IllegalStateException("Pexels 响应非 JSON");
        }
        JSONArray photos = root.getJSONArray("photos");
        List<Map<String, String>> list = new ArrayList<>();
        if (photos != null) {
            for (int i = 0; i < photos.size(); i++) {
                JSONObject photo = photos.getJSONObject(i);
                if (photo == null) {
                    continue;
                }
                JSONObject src = photo.getJSONObject("src");
                if (src == null) {
                    continue;
                }
                // large/original 为无水印直链
                String url = firstNonBlank(src.getString("large"), src.getString("large2x"), src.getString("original"));
                String thumb = firstNonBlank(src.getString("medium"), src.getString("small"), url);
                if (!StringUtils.hasText(url)) {
                    continue;
                }
                Map<String, String> item = new LinkedHashMap<>();
                item.put("url", url.trim());
                item.put("thumbUrl", StringUtils.hasText(thumb) ? thumb.trim() : url.trim());
                item.put("source", "PEXELS");
                String photographer = photo.getString("photographer");
                if (StringUtils.hasText(photographer)) {
                    item.put("photographer", photographer.trim());
                }
                String page = photo.getString("url");
                if (StringUtils.hasText(page)) {
                    item.put("pageUrl", page.trim());
                }
                list.add(item);
            }
        }
        if (list.size() < minCount()) {
            throw new IllegalStateException("图库结果不足 " + minCount() + " 张（当前 " + list.size() + "），可换关键词重试");
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
