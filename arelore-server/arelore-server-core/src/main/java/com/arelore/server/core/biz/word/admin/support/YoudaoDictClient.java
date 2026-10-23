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
 * 有道 jsonapi 轻量客户端：抽取配图、音标、释义、双语例句、词组、近义、派生，不落完整大包。
 */
public final class YoudaoDictClient {
    private static final int MAX_EXAMPLES = 3;
    private static final int MAX_PHRASES = 8;
    private static final int MAX_SYNONYM_GROUPS = 6;
    private static final int MAX_RELATED_GROUPS = 6;
    private static final int MAX_ETYM_CHARS = 800;
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

        // 双语例句 → 与词书 entry 相同结构 sentence.sentences[{sContent,sCn}]
        List<Map<String, String>> sentenceItems = extractBlngSentences(root.getJSONObject("blng_sents_part"));
        if (sentenceItems.isEmpty()) {
            sentenceItems = extractAuthSentences(root.getJSONObject("auth_sents_part"));
        }
        if (!sentenceItems.isEmpty()) {
            JSONObject sentence = new JSONObject();
            sentence.put("sentences", sentenceItems);
            ext.put("sentence", sentence);
        }

        List<Map<String, String>> phrases = extractPhrases(root.getJSONObject("phrs"));
        if (!phrases.isEmpty()) {
            ext.put("phrases", phrases);
        }

        List<Map<String, Object>> synonyms = extractSynonyms(root.getJSONObject("syno"));
        if (!synonyms.isEmpty()) {
            ext.put("synonyms", synonyms);
        }

        List<Map<String, Object>> relatedWords = extractRelatedWords(root.getJSONObject("rel_word"));
        if (!relatedWords.isEmpty()) {
            ext.put("relatedWords", relatedWords);
        }

        String etym = extractEtymSummary(root.getJSONObject("etym"));
        if (StringUtils.hasText(etym)) {
            ext.put("etym", etym);
        }
        return ext;
    }

    /** blng_sents_part.sentence-pair[] → [{sContent,sCn}] */
    private static List<Map<String, String>> extractBlngSentences(JSONObject blng) {
        List<Map<String, String>> list = new ArrayList<>();
        if (blng == null) {
            return list;
        }
        JSONArray pairs = blng.getJSONArray("sentence-pair");
        if (pairs == null || pairs.isEmpty()) {
            return list;
        }
        int limit = Math.min(pairs.size(), MAX_EXAMPLES);
        for (int i = 0; i < limit; i++) {
            JSONObject item = pairs.getJSONObject(i);
            if (item == null) {
                continue;
            }
            String en = firstNonBlank(
                stripHtml(item.getString("sentence")),
                stripHtml(item.getString("sentence-eng"))
            );
            String cn = stripHtml(item.getString("sentence-translation"));
            if (!StringUtils.hasText(en) && !StringUtils.hasText(cn)) {
                continue;
            }
            Map<String, String> row = new LinkedHashMap<>();
            row.put("sContent", StringUtils.hasText(en) ? en : "");
            row.put("sCn", StringUtils.hasText(cn) ? cn : "");
            list.add(row);
        }
        return list;
    }

    /** auth_sents_part.sent[] 仅英文权威句，作双语缺失时的兜底 */
    private static List<Map<String, String>> extractAuthSentences(JSONObject auth) {
        List<Map<String, String>> list = new ArrayList<>();
        if (auth == null) {
            return list;
        }
        JSONArray sents = auth.getJSONArray("sent");
        if (sents == null || sents.isEmpty()) {
            return list;
        }
        int limit = Math.min(sents.size(), 2);
        for (int i = 0; i < limit; i++) {
            JSONObject item = sents.getJSONObject(i);
            if (item == null) {
                continue;
            }
            String en = stripHtml(item.getString("foreign"));
            if (!StringUtils.hasText(en)) {
                continue;
            }
            Map<String, String> row = new LinkedHashMap<>();
            row.put("sContent", en);
            row.put("sCn", "");
            list.add(row);
        }
        return list;
    }

    /** phrs.phrs[].phr → [{en,cn}] */
    private static List<Map<String, String>> extractPhrases(JSONObject phrsRoot) {
        List<Map<String, String>> list = new ArrayList<>();
        if (phrsRoot == null) {
            return list;
        }
        JSONArray phrs = phrsRoot.getJSONArray("phrs");
        if (phrs == null || phrs.isEmpty()) {
            return list;
        }
        int limit = Math.min(phrs.size(), MAX_PHRASES);
        for (int i = 0; i < limit; i++) {
            JSONObject wrap = phrs.getJSONObject(i);
            if (wrap == null) {
                continue;
            }
            JSONObject phr = wrap.getJSONObject("phr");
            if (phr == null) {
                continue;
            }
            String en = extractNestedI(phr.getJSONObject("headword"));
            String cn = "";
            JSONArray trs = phr.getJSONArray("trs");
            if (trs != null && !trs.isEmpty()) {
                JSONObject tr0 = trs.getJSONObject(0);
                if (tr0 != null) {
                    cn = extractNestedI(tr0.getJSONObject("tr"));
                }
            }
            if (!StringUtils.hasText(en)) {
                continue;
            }
            Map<String, String> row = new LinkedHashMap<>();
            row.put("en", en.trim());
            if (StringUtils.hasText(cn)) {
                row.put("cn", cn.trim());
            }
            list.add(row);
        }
        return list;
    }

    /** syno.synos[] → [{pos,tran,words:[...]}] */
    private static List<Map<String, Object>> extractSynonyms(JSONObject synoRoot) {
        List<Map<String, Object>> list = new ArrayList<>();
        if (synoRoot == null) {
            return list;
        }
        JSONArray synos = synoRoot.getJSONArray("synos");
        if (synos == null || synos.isEmpty()) {
            return list;
        }
        int limit = Math.min(synos.size(), MAX_SYNONYM_GROUPS);
        for (int i = 0; i < limit; i++) {
            JSONObject wrap = synos.getJSONObject(i);
            if (wrap == null) {
                continue;
            }
            JSONObject syno = wrap.getJSONObject("syno");
            if (syno == null) {
                continue;
            }
            List<String> words = new ArrayList<>();
            JSONArray ws = syno.getJSONArray("ws");
            if (ws != null) {
                for (int j = 0; j < ws.size() && words.size() < 6; j++) {
                    JSONObject w = ws.getJSONObject(j);
                    if (w == null) {
                        continue;
                    }
                    String word = w.getString("w");
                    if (StringUtils.hasText(word)) {
                        words.add(word.trim());
                    }
                }
            }
            if (words.isEmpty()) {
                continue;
            }
            Map<String, Object> row = new LinkedHashMap<>();
            putIfTextMap(row, "pos", syno.getString("pos"));
            putIfTextMap(row, "tran", syno.getString("tran"));
            row.put("words", words);
            list.add(row);
        }
        return list;
    }

    /** rel_word.rels[] → [{pos, words:[{word,tran}]}] */
    private static List<Map<String, Object>> extractRelatedWords(JSONObject relRoot) {
        List<Map<String, Object>> list = new ArrayList<>();
        if (relRoot == null) {
            return list;
        }
        JSONArray rels = relRoot.getJSONArray("rels");
        if (rels == null || rels.isEmpty()) {
            return list;
        }
        int limit = Math.min(rels.size(), MAX_RELATED_GROUPS);
        for (int i = 0; i < limit; i++) {
            JSONObject wrap = rels.getJSONObject(i);
            if (wrap == null) {
                continue;
            }
            JSONObject rel = wrap.getJSONObject("rel");
            if (rel == null) {
                continue;
            }
            List<Map<String, String>> words = new ArrayList<>();
            JSONArray wordArr = rel.getJSONArray("words");
            if (wordArr != null) {
                for (int j = 0; j < wordArr.size() && words.size() < 8; j++) {
                    JSONObject w = wordArr.getJSONObject(j);
                    if (w == null) {
                        continue;
                    }
                    String word = w.getString("word");
                    if (!StringUtils.hasText(word)) {
                        continue;
                    }
                    Map<String, String> item = new LinkedHashMap<>();
                    item.put("word", word.trim());
                    String tran = w.getString("tran");
                    if (StringUtils.hasText(tran)) {
                        item.put("tran", tran.trim());
                    }
                    words.add(item);
                }
            }
            if (words.isEmpty()) {
                continue;
            }
            Map<String, Object> row = new LinkedHashMap<>();
            putIfTextMap(row, "pos", rel.getString("pos"));
            row.put("words", words);
            list.add(row);
        }
        return list;
    }

    /** etym.etyms.zh[0].value 截断摘要 */
    private static String extractEtymSummary(JSONObject etymRoot) {
        if (etymRoot == null) {
            return "";
        }
        JSONObject etyms = etymRoot.getJSONObject("etyms");
        if (etyms == null) {
            return "";
        }
        JSONArray zh = etyms.getJSONArray("zh");
        if (zh == null || zh.isEmpty()) {
            return "";
        }
        JSONObject first = zh.getJSONObject(0);
        if (first == null) {
            return "";
        }
        String value = first.getString("value");
        if (!StringUtils.hasText(value)) {
            return "";
        }
        String text = value.trim().replace("\r\n", "\n");
        // 学习卡只展示短摘要，避免整段词源刷屏
        if (text.length() > MAX_ETYM_CHARS) {
            int cut = text.indexOf('\n');
            if (cut > 40 && cut < MAX_ETYM_CHARS) {
                text = text.substring(0, cut).trim();
            } else {
                text = text.substring(0, MAX_ETYM_CHARS).trim() + "…";
            }
        }
        return text;
    }

    private static String extractNestedI(JSONObject node) {
        if (node == null) {
            return "";
        }
        JSONObject l = node.getJSONObject("l");
        if (l == null) {
            return firstNonBlank(node.getString("i"), "");
        }
        Object iNode = l.get("i");
        if (iNode instanceof String) {
            return ((String) iNode).trim();
        }
        if (iNode instanceof JSONArray) {
            JSONArray arr = (JSONArray) iNode;
            for (int j = 0; j < arr.size(); j++) {
                Object v = arr.get(j);
                if (v instanceof String && StringUtils.hasText((String) v)) {
                    return ((String) v).trim();
                }
            }
        }
        return "";
    }

    private static String stripHtml(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        return value.replaceAll("<[^>]+>", "").trim();
    }

    private static void putIfTextMap(Map<String, ?> map, String key, String value) {
        if (StringUtils.hasText(value) && map instanceof LinkedHashMap) {
            @SuppressWarnings("unchecked")
            Map<String, Object> m = (Map<String, Object>) map;
            m.put(key, value.trim());
        }
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
