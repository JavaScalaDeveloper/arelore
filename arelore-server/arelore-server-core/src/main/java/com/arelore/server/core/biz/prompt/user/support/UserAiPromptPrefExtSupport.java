package com.arelore.server.core.biz.prompt.user.support;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.arelore.server.core.biz.prompt.user.dto.UserAiPromptPrefResponse;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * user_ai_prompt_pref.ext_json 与 VO 字段互转。
 */
public final class UserAiPromptPrefExtSupport {
    public static final String STATUS_NORMAL = "NORMAL";
    public static final String STATUS_DELETED = "DELETED";

    public static final BigDecimal DEFAULT_FONT_SCALE = new BigDecimal("1.00");
    public static final int DEFAULT_FONT_SIZE_SP = 14;
    public static final String DEFAULT_COPY_SEP = "\n";
    public static final List<String> DEFAULT_COPY_FIELDS = List.of("PROMPT_TEXT");

    private static final Set<String> ALLOWED_COPY_FIELDS = new LinkedHashSet<>(Arrays.asList(
        "TITLE",
        "SUMMARY",
        "PROMPT_TEXT",
        "PUBLISH_STATUS",
        "CONFIG_TAGS",
        "MANUAL_TAGS",
        "PROMPT_UUID"
    ));

    private UserAiPromptPrefExtSupport() {
    }

    public static void fillVoFromExtJson(UserAiPromptPrefResponse response) {
        if (response == null) {
            return;
        }
        JSONObject ext = parseObject(response.getExtJson());
        response.setFontScale(readFontScale(ext));
        response.setFontSizeSp(readFontSizeSp(ext));
        response.setCopyFields(readCopyFields(ext));
        response.setCopySep(readCopySep(ext));
        String clientVersion = ext.getString("clientVersion");
        response.setClientVersion(StringUtils.hasText(clientVersion) ? clientVersion.trim() : null);
    }

    public static String buildExtJson(UserAiPromptPrefResponse request, String previousExtJson) {
        JSONObject ext = parseObject(previousExtJson);
        BigDecimal fontScale = request.getFontScale() != null
            ? normalizeFontScale(request.getFontScale())
            : readFontScale(ext);
        Integer fontSizeSp = request.getFontSizeSp() != null
            ? normalizeFontSizeSp(request.getFontSizeSp())
            : readFontSizeSp(ext);
        List<String> copyFields = request.getCopyFields() != null
            ? normalizeCopyFields(request.getCopyFields())
            : readCopyFields(ext);
        String copySep = request.getCopySep() != null
            ? normalizeCopySep(request.getCopySep())
            : readCopySep(ext);
        String clientVersion = request.getClientVersion() != null
            ? request.getClientVersion().trim()
            : ext.getString("clientVersion");

        JSONObject next = new JSONObject();
        next.put("fontScale", fontScale);
        next.put("fontSizeSp", fontSizeSp);
        next.put("copyFields", copyFields);
        next.put("copySep", copySep);
        if (StringUtils.hasText(clientVersion)) {
            next.put("clientVersion", clientVersion.trim());
        }
        return next.toJSONString();
    }

    public static String defaultExtJson() {
        JSONObject ext = new JSONObject();
        ext.put("fontScale", DEFAULT_FONT_SCALE);
        ext.put("fontSizeSp", DEFAULT_FONT_SIZE_SP);
        ext.put("copyFields", DEFAULT_COPY_FIELDS);
        ext.put("copySep", DEFAULT_COPY_SEP);
        return ext.toJSONString();
    }

    private static JSONObject parseObject(String raw) {
        if (!StringUtils.hasText(raw)) {
            return new JSONObject();
        }
        try {
            JSONObject obj = JSON.parseObject(raw);
            return obj == null ? new JSONObject() : obj;
        } catch (Exception e) {
            return new JSONObject();
        }
    }

    private static BigDecimal readFontScale(JSONObject ext) {
        BigDecimal v = ext.getBigDecimal("fontScale");
        return v == null ? DEFAULT_FONT_SCALE : normalizeFontScale(v);
    }

    private static Integer readFontSizeSp(JSONObject ext) {
        Integer v = ext.getInteger("fontSizeSp");
        return v == null ? DEFAULT_FONT_SIZE_SP : normalizeFontSizeSp(v);
    }

    private static List<String> readCopyFields(JSONObject ext) {
        JSONArray arr = ext.getJSONArray("copyFields");
        if (arr == null || arr.isEmpty()) {
            return new ArrayList<>(DEFAULT_COPY_FIELDS);
        }
        List<String> raw = new ArrayList<>();
        for (int i = 0; i < arr.size(); i++) {
            raw.add(arr.getString(i));
        }
        List<String> normalized = normalizeCopyFields(raw);
        return normalized.isEmpty() ? new ArrayList<>(DEFAULT_COPY_FIELDS) : normalized;
    }

    private static String readCopySep(JSONObject ext) {
        String sep = ext.getString("copySep");
        return sep == null ? DEFAULT_COPY_SEP : normalizeCopySep(sep);
    }

    private static BigDecimal normalizeFontScale(BigDecimal scale) {
        BigDecimal v = scale == null ? DEFAULT_FONT_SCALE : scale;
        if (v.compareTo(new BigDecimal("0.50")) < 0) {
            v = new BigDecimal("0.50");
        } else if (v.compareTo(new BigDecimal("2.00")) > 0) {
            v = new BigDecimal("2.00");
        }
        return v.setScale(2, RoundingMode.HALF_UP);
    }

    private static int normalizeFontSizeSp(Integer size) {
        int v = size == null ? DEFAULT_FONT_SIZE_SP : size;
        if (v < 10) {
            return 10;
        }
        if (v > 28) {
            return 28;
        }
        return v;
    }

    private static List<String> normalizeCopyFields(List<String> fields) {
        LinkedHashSet<String> set = new LinkedHashSet<>();
        if (fields != null) {
            for (String raw : fields) {
                if (!StringUtils.hasText(raw)) {
                    continue;
                }
                String code = raw.trim().toUpperCase(Locale.ROOT);
                if (ALLOWED_COPY_FIELDS.contains(code)) {
                    set.add(code);
                }
            }
        }
        return new ArrayList<>(set);
    }

    private static String normalizeCopySep(String sep) {
        if (sep == null) {
            return DEFAULT_COPY_SEP;
        }
        // 允许空串；长度限制避免异常写入
        String v = sep;
        if (v.length() > 16) {
            v = v.substring(0, 16);
        }
        return v;
    }
}
