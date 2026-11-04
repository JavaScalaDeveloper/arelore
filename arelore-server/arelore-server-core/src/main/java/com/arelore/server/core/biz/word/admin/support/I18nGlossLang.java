package com.arelore.server.core.biz.word.admin.support;

import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * 产品侧「多语种对译」支持的语种目录（背词 App 切换语言用）。
 * <p>
 * Wiktionary / FreeDictionary 会返回大量小语种（af、sq、ast…），产品只保留本枚举；
 * 码表以 ISO 639-1 为主，并归一化常见 639-3 / 变体（cmn→zh）。
 */
public enum I18nGlossLang {
    ZH("zh", "中文", "Chinese", "zh-CN-XiaoxiaoNeural"),
    JA("ja", "日语", "Japanese", "ja-JP-NanamiNeural"),
    KO("ko", "韩语", "Korean", "ko-KR-SunHiNeural"),
    ES("es", "西班牙语", "Spanish", "es-ES-ElviraNeural"),
    FR("fr", "法语", "French", "fr-FR-DeniseNeural"),
    DE("de", "德语", "German", "de-DE-KatjaNeural"),
    PT("pt", "葡萄牙语", "Portuguese", "pt-BR-FranciscaNeural"),
    RU("ru", "俄语", "Russian", "ru-RU-SvetlanaNeural"),
    VI("vi", "越南语", "Vietnamese", "vi-VN-HoaiMyNeural"),
    TH("th", "泰语", "Thai", "th-TH-PremwadeeNeural"),
    ID("id", "印尼语", "Indonesian", "id-ID-GadisNeural"),
    AR("ar", "阿拉伯语", "Arabic", "ar-SA-ZariyahNeural"),
    HI("hi", "印地语", "Hindi", "hi-IN-SwaraNeural"),
    IT("it", "意大利语", "Italian", "it-IT-ElsaNeural"),
    NL("nl", "荷兰语", "Dutch", "nl-NL-FennaNeural"),
    TR("tr", "土耳其语", "Turkish", "tr-TR-EmelNeural"),
    PL("pl", "波兰语", "Polish", "pl-PL-AgnieszkaNeural"),
    SV("sv", "瑞典语", "Swedish", "sv-SE-SofieNeural"),
    UK("uk", "乌克兰语", "Ukrainian", "uk-UA-PolinaNeural"),
    MS("ms", "马来语", "Malay", "ms-MY-YasminNeural"),
    EN("en", "英语", "English", "en-US-JennyNeural");

    private final String code;
    private final String labelZh;
    private final String labelEn;
    private final String edgeVoice;

    private static final Map<String, I18nGlossLang> BY_CODE;

    static {
        Map<String, I18nGlossLang> m = new LinkedHashMap<>();
        for (I18nGlossLang lang : values()) {
            m.put(lang.code, lang);
        }
        BY_CODE = Collections.unmodifiableMap(m);
    }

    I18nGlossLang(String code, String labelZh, String labelEn, String edgeVoice) {
        this.code = code;
        this.labelZh = labelZh;
        this.labelEn = labelEn;
        this.edgeVoice = edgeVoice;
    }

    public String getCode() {
        return code;
    }

    public String getLabelZh() {
        return labelZh;
    }

    public String getLabelEn() {
        return labelEn;
    }

    public String getEdgeVoice() {
        return edgeVoice;
    }

    public static Optional<I18nGlossLang> ofCode(String raw) {
        String code = canonicalize(raw);
        if (!StringUtils.hasText(code)) {
            return Optional.empty();
        }
        return Optional.ofNullable(BY_CODE.get(code));
    }

    public static boolean isSupported(String raw) {
        return ofCode(raw).isPresent();
    }

    public static String labelZhOrFallback(String raw, String fallback) {
        return ofCode(raw).map(I18nGlossLang::getLabelZh)
            .orElse(StringUtils.hasText(fallback) ? fallback.trim() : (raw == null ? "" : raw.trim()));
    }

    public static String edgeVoiceOrNull(String raw) {
        return ofCode(raw).map(I18nGlossLang::getEdgeVoice).orElse(null);
    }

    /**
     * 归一化 Wiktionary / 请求语种码到产品短码；无法识别时返回小写短码（可能不被 isSupported）。
     */
    public static String canonicalize(String languageCode) {
        if (!StringUtils.hasText(languageCode)) {
            return "";
        }
        String c = languageCode.trim().toLowerCase(Locale.ROOT);
        if (c.contains("-")) {
            c = c.substring(0, c.indexOf('-'));
        }
        if (c.contains("_")) {
            c = c.substring(0, c.indexOf('_'));
        }
        // 常见 639-3 / Wiktionary 变体 → 639-1
        return switch (c) {
            case "cmn", "zho", "chi", "zh" -> "zh";
            case "eng", "en" -> "en";
            case "jpn", "ja" -> "ja";
            case "kor", "ko" -> "ko";
            case "spa", "es" -> "es";
            case "fra", "fre", "fr" -> "fr";
            case "deu", "ger", "de" -> "de";
            case "por", "pt" -> "pt";
            case "rus", "ru" -> "ru";
            case "vie", "vi" -> "vi";
            case "tha", "th" -> "th";
            case "ind", "id" -> "id";
            case "ara", "ar" -> "ar";
            case "hin", "hi" -> "hi";
            case "ita", "it" -> "it";
            case "nld", "dut", "nl" -> "nl";
            case "tur", "tr" -> "tr";
            case "pol", "pl" -> "pl";
            case "swe", "sv" -> "sv";
            case "ukr", "uk" -> "uk";
            case "msa", "may", "ms" -> "ms";
            default -> c;
        };
    }

    public static Map<String, String> codeToLabelZh() {
        Map<String, String> m = new LinkedHashMap<>();
        for (I18nGlossLang lang : values()) {
            m.put(lang.code, lang.labelZh);
        }
        return Collections.unmodifiableMap(m);
    }

    /** 产品语种声明顺序（补词典落库顺序） */
    public static java.util.List<String> productCodesInOrder() {
        return Arrays.stream(values()).map(I18nGlossLang::getCode).toList();
    }
}
