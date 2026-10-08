package com.arelore.server.core.biz.word.admin.support;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
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
 * 有道 jsonapi 轻量客户端：只抽取配图/音标，不落完整大包。
 */
public final class YoudaoDictClient {
    private static final String JSONAPI = "https://dict.youdao.com/jsonapi?q=";
    private static final HttpClient CLIENT = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(8))
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build();

    private YoudaoDictClient() {
    }

    public static JSONObject fetchSlimExt(String word) throws Exception {
        if (!StringUtils.hasText(word)) {
            throw new IllegalArgumentException("word 为空");
        }
        String q = URLEncoder.encode(word.trim(), StandardCharsets.UTF_8);
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(JSONAPI + q))
            .timeout(Duration.ofSeconds(15))
            .header("User-Agent", "arelore-admin/1.0")
            .GET()
            .build();
        HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("有道接口 HTTP " + response.statusCode());
        }
        String body = response.body();
        if (!StringUtils.hasText(body)) {
            throw new IllegalStateException("有道接口空响应");
        }
        JSONObject root = JSON.parseObject(body);
        if (root == null) {
            throw new IllegalStateException("有道响应非 JSON");
        }
        return toSlimExt(root);
    }

    static JSONObject toSlimExt(JSONObject root) {
        JSONObject ext = new JSONObject();
        ext.put("source", "YOUDAO");
        List<Map<String, String>> pictures = new ArrayList<>();
        JSONObject picDict = root.getJSONObject("pic_dict");
        if (picDict != null) {
            JSONArray pics = picDict.getJSONArray("pic");
            if (pics != null) {
                for (int i = 0; i < pics.size(); i++) {
                    JSONObject pic = pics.getJSONObject(i);
                    if (pic == null) {
                        continue;
                    }
                    String url = firstNonBlank(pic.getString("url"), pic.getString("image"));
                    url = normalizePictureUrl(url);
                    if (!StringUtils.hasText(url)) {
                        continue;
                    }
                    Map<String, String> item = new LinkedHashMap<>();
                    item.put("url", url);
                    pictures.add(item);
                }
            }
        }
        ext.put("pictures", pictures);

        JSONObject simple = root.getJSONObject("simple");
        if (simple != null) {
            JSONArray words = simple.getJSONArray("word");
            if (words != null && !words.isEmpty()) {
                JSONObject w0 = words.getJSONObject(0);
                if (w0 != null) {
                    putIfText(ext, "ukphone", w0.getString("ukphone"));
                    putIfText(ext, "usphone", w0.getString("usphone"));
                }
            }
        }
        if (!ext.containsKey("ukphone") || !ext.containsKey("usphone")) {
            JSONObject ec = root.getJSONObject("ec");
            if (ec != null) {
                JSONArray words = ec.getJSONArray("word");
                if (words != null && !words.isEmpty()) {
                    JSONObject w0 = words.getJSONObject(0);
                    if (w0 != null) {
                        if (!ext.containsKey("ukphone")) {
                            putIfText(ext, "ukphone", w0.getString("ukphone"));
                        }
                        if (!ext.containsKey("usphone")) {
                            putIfText(ext, "usphone", w0.getString("usphone"));
                        }
                    }
                }
            }
        }
        return ext;
    }

    public static String normalizePictureUrl(String url) {
        if (!StringUtils.hasText(url)) {
            return "";
        }
        String value = url.trim();
        // 去掉有道尾巴问号
        if (value.endsWith("?")) {
            value = value.substring(0, value.length() - 1);
        }
        value = value
            .replace("http://ydschool-online.nos.netease.com/", "https://ydschool-online.nosdn.127.net/")
            .replace("https://ydschool-online.nos.netease.com/", "https://ydschool-online.nosdn.127.net/");
        if (value.startsWith("//")) {
            value = "https:" + value;
        } else if (value.startsWith("http://")) {
            value = "https://" + value.substring("http://".length());
        }
        return value;
    }

    private static void putIfText(JSONObject ext, String key, String value) {
        if (StringUtils.hasText(value)) {
            ext.put(key, value.trim());
        }
    }

    private static String firstNonBlank(String a, String b) {
        if (StringUtils.hasText(a)) {
            return a;
        }
        return b;
    }
}
