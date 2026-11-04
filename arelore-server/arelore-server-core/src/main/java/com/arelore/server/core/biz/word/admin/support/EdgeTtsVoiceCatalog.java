package com.arelore.server.core.biz.word.admin.support;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Edge Neural 音色目录：委托 {@link I18nGlossLang}，避免与产品语种表双份维护。
 */
public final class EdgeTtsVoiceCatalog {
    private EdgeTtsVoiceCatalog() {
    }

    public static String voiceForLang(String langCode) {
        return I18nGlossLang.edgeVoiceOrNull(langCode);
    }

    public static Map<String, String> all() {
        Map<String, String> m = new LinkedHashMap<>();
        for (I18nGlossLang lang : I18nGlossLang.values()) {
            m.put(lang.getCode(), lang.getEdgeVoice());
        }
        return Collections.unmodifiableMap(m);
    }
}
