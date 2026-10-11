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
 * 有道 jsonapi 轻量客户端：抽取配图、音标、一词多义释义，不落完整大包。
 */
public final class YoudaoDictClient {
    private static final java.util.regex.Pattern POS_PREFIX =
        java.util.regex.Pattern.compile("^([a-zA-Z]+\\.?|【[^】]+】)\\s*(.*)$");
    private static final String JSONAPI = "https://dict.youdao.com/jsonapi?q=";
    private static final HttpClient CLIENT = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(8))
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build();

    private YoudaoDictClient() {
    }

    public static JSONObject fetchSlimExt(String word) throws Exception {
        return toSlimExt(fetchRoot(word).root);
    }

    /** 管理端调试用：返回 HTTP 状态、耗时、完整 JSON 根节点（勿直接落库） */
    public static RawFetch fetchRoot(String word) throws Exception {
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
        long start = System.currentTimeMillis();
        HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        long elapsedMs = System.currentTimeMillis() - start;
        RawFetch raw = new RawFetch();
        raw.httpStatus = response.statusCode();
        raw.elapsedMs = elapsedMs;
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
        raw.root = root;
        return raw;
    }

    public static final class RawFetch {
        public int httpStatus;
        public long elapsedMs;
        public JSONObject root;
    }

    public static JSONObject toSlimExt(JSONObject root) {
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
        for (Map<String, String> pic : pictures) {
            pic.put("source", "YOUDAO");
        }

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
                    List<Map<String, String>> meanings = extractMeanings(w0.getJSONArray("trs"));
                    if (!meanings.isEmpty()) {
                        ext.put("meanings", meanings);
                    }
                }
            }
        }
        return ext;
    }

    /** 从 ec.word[0].trs 抽取一词多义：[{pos,trans}, ...] */
    public static List<Map<String, String>> extractMeanings(JSONArray trs) {
        List<Map<String, String>> list = new ArrayList<>();
        if (trs == null || trs.isEmpty()) {
            return list;
        }
        for (int i = 0; i < trs.size(); i++) {
            JSONObject item = trs.getJSONObject(i);
            if (item == null) {
                continue;
            }
            String raw = firstMeaningText(item);
            if (!StringUtils.hasText(raw)) {
                continue;
            }
            String text = raw.trim();
            String pos = "";
            String trans = text;
            java.util.regex.Matcher matcher = POS_PREFIX.matcher(text);
            if (matcher.matches()) {
                pos = matcher.group(1) == null ? "" : matcher.group(1).trim();
                trans = matcher.group(2) == null ? "" : matcher.group(2).trim();
                if (!StringUtils.hasText(trans)) {
                    trans = text;
                    pos = "";
                }
            }
            Map<String, String> meaning = new LinkedHashMap<>();
            if (StringUtils.hasText(pos)) {
                meaning.put("pos", pos);
            }
            meaning.put("trans", trans);
            list.add(meaning);
        }
        return list;
    }

    private static String firstMeaningText(JSONObject trItem) {
        JSONArray tr = trItem.getJSONArray("tr");
        if (tr == null || tr.isEmpty()) {
            return firstNonBlank(trItem.getString("tran"), trItem.getString("tranCn"));
        }
        JSONObject tr0 = tr.getJSONObject(0);
        if (tr0 == null) {
            return "";
        }
        JSONObject l = tr0.getJSONObject("l");
        if (l != null) {
            JSONArray iArr = l.getJSONArray("i");
            if (iArr != null) {
                for (int j = 0; j < iArr.size(); j++) {
                    Object v = iArr.get(j);
                    if (v instanceof String && StringUtils.hasText((String) v)) {
                        return ((String) v).trim();
                    }
                }
            }
        }
        return firstNonBlank(tr0.getString("tran"), tr0.getString("tranCn"));
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
