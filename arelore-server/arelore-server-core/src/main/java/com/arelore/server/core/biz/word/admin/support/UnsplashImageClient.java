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
 * Unsplash 免费图库：无水印、可免费使用（需保留摄影师署名）。
 * 申请 Access Key：https://unsplash.com/developers
 * 文档：https://unsplash.com/documentation
 */
@Component
public class UnsplashImageClient {
    private static final String SEARCH_URL = "https://api.unsplash.com/search/photos";
    private static final HttpClient CLIENT = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(8))
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build();

    @Value("${arelore.word.image.unsplash-access-key:}")
    private String accessKey;

    @Value("${arelore.word.image.min-count:4}")
    private int minCount;

    @Value("${arelore.word.image.search-count:12}")
    private int searchCount;

    public boolean configured() {
        return StringUtils.hasText(accessKey);
    }

    public int minCount() {
        return Math.max(4, minCount);
    }

    public List<Map<String, String>> search(String query) throws Exception {
        if (!configured()) {
            throw new IllegalStateException("未配置 arelore.word.image.unsplash-access-key（可用环境变量 ARELORE_UNSPLASH_ACCESS_KEY）");
        }
        if (!StringUtils.hasText(query)) {
            throw new IllegalArgumentException("搜索词为空");
        }
        int perPage = Math.max(minCount(), Math.min(30, searchCount));
        String q = URLEncoder.encode(query.trim(), StandardCharsets.UTF_8);
        URI uri = URI.create(SEARCH_URL + "?query=" + q + "&per_page=" + perPage + "&orientation=landscape&content_filter=high");
        HttpRequest request = HttpRequest.newBuilder()
            .uri(uri)
            .timeout(Duration.ofSeconds(20))
            .header("Authorization", "Client-ID " + accessKey.trim())
            .header("Accept-Version", "v1")
            .header("User-Agent", "arelore-admin/1.0")
            .GET()
            .build();
        HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() == 401 || response.statusCode() == 403) {
            throw new IllegalStateException("Unsplash Access Key 无效或无权限");
        }
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("Unsplash HTTP " + response.statusCode());
        }
        JSONObject root = JSON.parseObject(response.body());
        if (root == null) {
            throw new IllegalStateException("Unsplash 响应非 JSON");
        }
        JSONArray results = root.getJSONArray("results");
        List<Map<String, String>> list = new ArrayList<>();
        if (results != null) {
            for (int i = 0; i < results.size(); i++) {
                JSONObject photo = results.getJSONObject(i);
                if (photo == null) {
                    continue;
                }
                JSONObject urls = photo.getJSONObject("urls");
                if (urls == null) {
                    continue;
                }
                // regular/full 为无水印直链（按 Unsplash 指南 hotlink）
                String url = firstNonBlank(urls.getString("regular"), urls.getString("full"), urls.getString("small"));
                String thumb = firstNonBlank(urls.getString("small"), urls.getString("thumb"), url);
                if (!StringUtils.hasText(url)) {
                    continue;
                }
                Map<String, String> item = new LinkedHashMap<>();
                item.put("url", url.trim());
                item.put("thumbUrl", StringUtils.hasText(thumb) ? thumb.trim() : url.trim());
                item.put("source", "UNSPLASH");
                JSONObject user = photo.getJSONObject("user");
                if (user != null && StringUtils.hasText(user.getString("name"))) {
                    item.put("photographer", user.getString("name").trim());
                }
                String page = photo.getString("links") != null
                    ? photo.getJSONObject("links").getString("html")
                    : null;
                if (StringUtils.hasText(page)) {
                    item.put("pageUrl", page.trim());
                }
                // Unsplash 要求触发 download 端点时才算正式下载；候选预览用 urls 即可
                String downloadLocation = photo.getJSONObject("links") != null
                    ? photo.getJSONObject("links").getString("download_location")
                    : null;
                if (StringUtils.hasText(downloadLocation)) {
                    item.put("downloadLocation", downloadLocation.trim());
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
