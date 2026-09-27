package com.arelore.server.core.biz.word.admin.support;

import com.arelore.server.core.common.exception.BusinessException;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

public final class WordCodes {
    public static final Pattern PATTERN = Pattern.compile("^[A-Z0-9_]+$");

    private WordCodes() {
    }

    public static void require(String field, String value) {
        if (!StringUtils.hasText(value) || !PATTERN.matcher(value.trim()).matches()) {
            throw new BusinessException(field + " 只能由大写字母、数字和下划线组成");
        }
    }
}
