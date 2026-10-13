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

        JSONObject simpleWord = firstWordObject(root.getJSONObject("simple"));
        if (simpleWord != null) {
            putIfText(ext, "ukphone", simpleWord.getString("ukphone"));
            putIfText(ext, "usphone", simpleWord.getString("usphone"));
            // phone 也可能是音标兜底
            if (!ext.containsKey("ukphone") && !ext.containsKey("usphone")) {
                putIfText(ext, "usphone", simpleWord.getString("phone"));
            }
        }
        // 释义优先级：英汉 ec → 汉英 ce → 网络释义 → 英英 ee → 机器翻译
        // 尽量保证无空格单词都能落到至少一条 meanings
        List<Map<String, String>> meanings = extractEcMeanings(root.getJSONObject("ec"));
        if (meanings.isEmpty()) {
            meanings = extractEcMeanings(root.getJSONObject("ce"));
        }
        if (meanings.isEmpty()) {
            meanings = extractWebMeanings(root.getJSONObject("web_trans"));
        }
        if (meanings.isEmpty()) {
            meanings = extractEeMeanings(root.getJSONObject("ee"));
        }
        if (meanings.isEmpty()) {
            meanings = extractFanyiMeanings(root.getJSONObject("fanyi"));
        }
        if (!meanings.isEmpty()) {
            ext.put("meanings", meanings);
        }
        JSONObject ecWord = firstWordObject(root.getJSONObject("ec"));
        if (ecWord != null) {
            if (!ext.containsKey("ukphone")) {
                putIfText(ext, "ukphone", ecWord.getString("ukphone"));
            }
            if (!ext.containsKey("usphone")) {
                putIfText(ext, "usphone", ecWord.getString("usphone"));
            }
        }
        return ext;
    }

    /**
     * 有道 word 字段可能是数组或单对象，统一取首个词条对象。
     */
    private static JSONObject firstWordObject(JSONObject parent) {
        if (parent == null) {
            return null;
        }
        Object wordNode = parent.get("word");
        if (wordNode instanceof JSONObject) {
            return (JSONObject) wordNode;
        }
        if (wordNode instanceof JSONArray) {
            JSONArray words = (JSONArray) wordNode;
            if (words.isEmpty()) {
                return null;
            }
            return words.getJSONObject(0);
        }
        return null;
    }

    private static List<Map<String, String>> extractEcMeanings(JSONObject ec) {
        JSONObject w0 = firstWordObject(ec);
        if (w0 == null) {
            return List.of();
        }
        return extractMeanings(w0.getJSONArray("trs"));
    }

    /** 英英释义 ee.word.trs（无中文时兜底，保证有释义文本） */
    private static List<Map<String, String>> extractEeMeanings(JSONObject ee) {
        JSONObject w0 = firstWordObject(ee);
        if (w0 == null) {
            return List.of();
        }
        return extractMeanings(w0.getJSONArray("trs"));
    }

    /** web_trans.web-translation[*].trans[*].value */
    private static List<Map<String, String>> extractWebMeanings(JSONObject webTrans) {
        List<Map<String, String>> list = new ArrayList<>();
        if (webTrans == null) {
            return list;
        }
        JSONArray items = webTrans.getJSONArray("web-translation");
        if (items == null || items.isEmpty()) {
            return list;
        }
        for (int i = 0; i < items.size() && list.size() < 6; i++) {
            JSONObject item = items.getJSONObject(i);
            if (item == null) {
                continue;
            }
            JSONArray trans = item.getJSONArray("trans");
            if (trans == null) {
                continue;
            }
            for (int j = 0; j < trans.size() && list.size() < 6; j++) {
                JSONObject t = trans.getJSONObject(j);
                if (t == null) {
                    continue;
                }
                String value = t.getString("value");
                if (!StringUtils.hasText(value)) {
                    continue;
                }
                Map<String, String> meaning = new LinkedHashMap<>();
                meaning.put("trans", value.trim());
                list.add(meaning);
            }
        }
        return list;
    }

    private static List<Map<String, String>> extractFanyiMeanings(JSONObject fanyi) {
        if (fanyi == null) {
            return List.of();
        }
        String tran = fanyi.getString("tran");
        if (!StringUtils.hasText(tran)) {
            return List.of();
        }
        Map<String, String> meaning = new LinkedHashMap<>();
        meaning.put("trans", tran.trim());
        return List.of(meaning);
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
            // 新版字段：trs[].pos + trs[].tran；汉英 ce 常见 #tran
            String directPos = item.getString("pos");
            String directTran = firstNonBlank(
                item.getString("tran"),
                firstNonBlank(item.getString("tranCn"), item.getString("#tran"))
            );
            if (StringUtils.hasText(directTran) && item.getJSONArray("tr") == null) {
                Map<String, String> meaning = new LinkedHashMap<>();
                if (StringUtils.hasText(directPos)) {
                    meaning.put("pos", directPos.trim());
                }
                meaning.put("trans", directTran.trim());
                list.add(meaning);
                continue;
            }
            String raw = firstMeaningText(item);
            if (!StringUtils.hasText(raw)) {
                continue;
            }
            String text = raw.trim();
            String pos = StringUtils.hasText(directPos) ? directPos.trim() : "";
            String trans = text;
            java.util.regex.Matcher matcher = POS_PREFIX.matcher(text);
            if (matcher.matches()) {
                if (!StringUtils.hasText(pos)) {
                    pos = matcher.group(1) == null ? "" : matcher.group(1).trim();
                }
                trans = matcher.group(2) == null ? "" : matcher.group(2).trim();
                if (!StringUtils.hasText(trans)) {
                    trans = text;
                    if (!StringUtils.hasText(directPos)) {
                        pos = "";
                    }
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
            // i 可能是字符串（ee）或字符串数组（ec）
            Object iNode = l.get("i");
            if (iNode instanceof String && StringUtils.hasText((String) iNode)) {
                return ((String) iNode).trim();
            }
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
