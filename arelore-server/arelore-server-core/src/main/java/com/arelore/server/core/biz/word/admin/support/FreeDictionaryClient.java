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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * FreeDictionaryAPI（Wiktionary）：英英释义 + 可选多语种译词。
 * 文档：https://freedictionaryapi.com/ ；限流约 1000 次/小时/IP。
 * <p>
 * 有道 jsonapi 以中英为主；本客户端补齐国际化译词（wordTranslations）与英英释义兜底。
 * 译词语种只保留 {@link I18nGlossLang} 产品目录，丢弃 Wiktionary 长尾小语种。
 */
public final class FreeDictionaryClient {
    private static final String API =
        "https://freedictionaryapi.com/api/v1/entries/%s/%s?translations=true";
    private static final int MAX_MEANINGS = 8;
    private static final int MAX_WORDS_PER_LANG = 5;
    private static final HttpClient CLIENT = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(8))
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build();

    private FreeDictionaryClient() {
    }

    /** FreeDictionary 小时级限流 */
    public static final class RateLimitedException extends Exception {
        public RateLimitedException(String message) {
            super(message);
        }
    }

    /**
     * @param languageCode 词条语种，空则按 en
     */
    public static JSONObject fetchSlimExt(String word, String languageCode) throws Exception {
        if (!StringUtils.hasText(word)) {
            throw new IllegalArgumentException("word 为空");
        }
        String lang = normalizeLang(languageCode);
        String q = URLEncoder.encode(word.trim(), StandardCharsets.UTF_8);
        String url = String.format(API, lang, q);
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .timeout(Duration.ofSeconds(20))
            .header("User-Agent", "Mozilla/5.0 (compatible; arelore-admin/1.0)")
            .header("Accept", "application/json")
            .GET()
            .build();
        HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() == 404) {
            return emptySlim();
        }
        if (response.statusCode() == 429) {
            throw new RateLimitedException("FreeDictionary 限流(429)，请稍后再用 no_freedictionary 标签补同步");
        }
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("FreeDictionary HTTP " + response.statusCode());
        }
        JSONObject root = JSON.parseObject(response.body());
        if (root == null) {
            throw new IllegalStateException("FreeDictionary 响应非 JSON");
        }
        return toSlimExt(root);
    }

    public static JSONObject toSlimExt(JSONObject root) {
        JSONObject ext = emptySlim();
        if (root == null) {
            return ext;
        }
        List<Map<String, String>> meanings = new ArrayList<>();
        // langCode -> ordered unique words
        Map<String, LinkedHashSet<String>> byLang = new LinkedHashMap<>();
        Map<String, String> langNames = new LinkedHashMap<>();

        JSONArray entries = root.getJSONArray("entries");
        if (entries != null) {
            for (int i = 0; i < entries.size(); i++) {
                JSONObject entry = entries.getJSONObject(i);
                if (entry == null) {
                    continue;
                }
                String pos = entry.getString("partOfSpeech");
                collectSenses(entry.getJSONArray("senses"), pos, meanings, byLang, langNames);
            }
        }
        if (!meanings.isEmpty()) {
            ext.put("meanings", meanings.size() > MAX_MEANINGS ? meanings.subList(0, MAX_MEANINGS) : meanings);
        }
        List<Map<String, Object>> translations = toTranslationList(byLang, langNames);
        if (!translations.isEmpty()) {
            ext.put("wordTranslations", translations);
        }
        JSONObject source = root.getJSONObject("source");
        if (source != null && StringUtils.hasText(source.getString("url"))) {
            ext.put("wiktionaryUrl", source.getString("url"));
        }
        return ext;
    }

    private static JSONObject emptySlim() {
        JSONObject ext = new JSONObject();
        ext.put("source", "FREEDICTIONARY");
        return ext;
    }

    private static void collectSenses(
        JSONArray senses,
        String pos,
        List<Map<String, String>> meanings,
        Map<String, LinkedHashSet<String>> byLang,
        Map<String, String> langNames
    ) {
        if (senses == null || senses.isEmpty()) {
            return;
        }
        for (int i = 0; i < senses.size(); i++) {
            JSONObject sense = senses.getJSONObject(i);
            if (sense == null) {
                continue;
            }
            String def = sense.getString("definition");
            if (StringUtils.hasText(def) && meanings.size() < MAX_MEANINGS) {
                Map<String, String> m = new LinkedHashMap<>();
                m.put("pos", StringUtils.hasText(pos) ? pos.trim() : "");
                m.put("text", def.trim());
                meanings.add(m);
            }
            harvestTranslations(sense.getJSONArray("translations"), byLang, langNames);
            collectSenses(sense.getJSONArray("subsenses"), pos, meanings, byLang, langNames);
        }
    }

    private static void harvestTranslations(
        JSONArray translations,
        Map<String, LinkedHashSet<String>> byLang,
        Map<String, String> langNames
    ) {
        if (translations == null || translations.isEmpty()) {
            return;
        }
        for (int i = 0; i < translations.size(); i++) {
            JSONObject item = translations.getJSONObject(i);
            if (item == null) {
                continue;
            }
            JSONObject language = item.getJSONObject("language");
            String code = language == null ? "" : language.getString("code");
            String name = language == null ? "" : language.getString("name");
            String word = item.getString("word");
            code = I18nGlossLang.canonicalize(code);
            // 只收产品目录语种（丢弃 af/sq/ast 等长尾）
            if (!I18nGlossLang.isSupported(code) || !StringUtils.hasText(word)) {
                continue;
            }
            LinkedHashSet<String> words = byLang.computeIfAbsent(code, k -> new LinkedHashSet<>());
            if (words.size() >= MAX_WORDS_PER_LANG) {
                continue;
            }
            words.add(word.trim());
            langNames.putIfAbsent(code, I18nGlossLang.labelZhOrFallback(code, name));
        }
    }

    private static List<Map<String, Object>> toTranslationList(
        Map<String, LinkedHashSet<String>> byLang,
        Map<String, String> langNames
    ) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (String code : I18nGlossLang.productCodesInOrder()) {
            LinkedHashSet<String> words = byLang.get(code);
            if (words == null || words.isEmpty()) {
                continue;
            }
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("lang", code);
            row.put("langName", langNames.getOrDefault(code, I18nGlossLang.labelZhOrFallback(code, code)));
            row.put("words", new ArrayList<>(words));
            list.add(row);
        }
        return list;
    }

    /** 词条查询语种：默认 en */
    private static String normalizeLang(String languageCode) {
        String code = I18nGlossLang.canonicalize(languageCode);
        return StringUtils.hasText(code) ? code : "en";
    }
}
