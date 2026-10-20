package com.arelore.server.core.biz.word.admin.support;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * 把单词释义转成适合整段 TTS 的中文朗读稿（词性读中文名，避免 n./adj. 被念成字母）。
 * 优先读 base_info.ext_info.meanings（有道落库结构），兼容 entry 侧 trans。
 */
public final class MeaningSpeechTextBuilder {
    private static final Map<String, String> POS_CN = new LinkedHashMap<>();

    static {
        POS_CN.put("n", "名词");
        POS_CN.put("v", "动词");
        POS_CN.put("vt", "及物动词");
        POS_CN.put("vi", "不及物动词");
        POS_CN.put("adj", "形容词");
        POS_CN.put("adv", "副词");
        POS_CN.put("prep", "介词");
        POS_CN.put("pron", "代词");
        POS_CN.put("conj", "连词");
        POS_CN.put("art", "冠词");
        POS_CN.put("num", "数词");
        POS_CN.put("int", "感叹词");
        POS_CN.put("aux", "助动词");
        POS_CN.put("modal", "情态动词");
        POS_CN.put("det", "限定词");
        POS_CN.put("abbr", "缩写");
        POS_CN.put("phrase", "短语");
        POS_CN.put("idiom", "习语");
        POS_CN.put("pl", "复数");
        POS_CN.put("pref", "前缀");
        POS_CN.put("suf", "后缀");
        POS_CN.put("part", "分词");
        POS_CN.put("ger", "动名词");
        POS_CN.put("inf", "不定式");
        POS_CN.put("link", "连系动词");
        POS_CN.put("quant", "量词");
        POS_CN.put("ord", "序数词");
        POS_CN.put("count", "可数名词");
        POS_CN.put("uncount", "不可数名词");
    }

    private MeaningSpeechTextBuilder() {
    }

    /** 从 base_info.ext_info 生成朗读稿 */
    public static String fromExt(JSONObject ext) {
        if (ext == null) {
            return "";
        }
        JSONArray items = ext.getJSONArray("meanings");
        if (items == null || items.isEmpty()) {
            items = ext.getJSONArray("trans");
        }
        if (items == null || items.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            JSONObject item = items.getJSONObject(i);
            if (item == null) {
                continue;
            }
            String cn = firstNonBlank(
                item.getString("trans"),
                item.getString("tranCn"),
                item.getString("tranOther"),
                item.getString("tran")
            );
            if (!StringUtils.hasText(cn)) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append("。");
            }
            String posSpeech = posToSpeech(item.getString("pos"));
            if (StringUtils.hasText(posSpeech)) {
                sb.append(posSpeech).append("。");
            }
            sb.append(cleanGloss(cn.trim()));
        }
        return sb.toString().trim();
    }

    private static String posToSpeech(String posRaw) {
        if (!StringUtils.hasText(posRaw)) {
            return "";
        }
        String pos = posRaw.trim().toLowerCase(Locale.ROOT)
            .replace("&amp;", "&")
            .replace(".", "");
        if (!StringUtils.hasText(pos)) {
            return "";
        }
        String[] parts = pos.split("[&/+,，、\\s]+");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (!StringUtils.hasText(part)) {
                continue;
            }
            String key = part.trim().toLowerCase(Locale.ROOT);
            String cn = POS_CN.get(key);
            if (!StringUtils.hasText(cn)) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append("、");
            }
            sb.append(cn);
        }
        return sb.toString();
    }

    private static String cleanGloss(String gloss) {
        return gloss
            .replace('；', '，')
            .replace(';', '，')
            .replaceAll("[()（）\\[\\]]", "")
            .replaceAll("\\s+", "")
            .trim();
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return "";
        }
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value.trim();
            }
        }
        return "";
    }
}
