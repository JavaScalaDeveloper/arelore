package com.arelore.server.core.biz.word.user.support;

import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.math.BigDecimal;

/**
 * 用户业务 ID 字符串辅助：VO 填充 idStr，请求侧 idStr → userId。
 */
public final class WordUserIdStrSupport {
    private WordUserIdStrSupport() {
    }

    public static void fillIdStr(Object response) {
        if (response == null) {
            return;
        }
        try {
            Method getUserId = response.getClass().getMethod("getUserId");
            Object userId = getUserId.invoke(response);
            if (userId instanceof BigDecimal bd) {
                Method setIdStr = response.getClass().getMethod("setIdStr", String.class);
                setIdStr.invoke(response, bd.toPlainString());
            }
        } catch (ReflectiveOperationException ignored) {
            // VO 无对应方法时忽略
        }
    }

    /**
     * 若 userId 为空且带了 idStr，则解析写入 userId，供查询条件使用。
     */
    public static void applyIdStrToUserId(Object request) {
        if (request == null) {
            return;
        }
        try {
            Method getUserId = request.getClass().getMethod("getUserId");
            if (getUserId.invoke(request) != null) {
                return;
            }
            Method getIdStr = request.getClass().getMethod("getIdStr");
            Object idStrObj = getIdStr.invoke(request);
            if (!(idStrObj instanceof String idStr) || !StringUtils.hasText(idStr)) {
                return;
            }
            Method setUserId = request.getClass().getMethod("setUserId", BigDecimal.class);
            setUserId.invoke(request, new BigDecimal(idStr.trim()));
        } catch (NumberFormatException | ReflectiveOperationException ignored) {
            // 非法 idStr 时保持原样，查询自然无结果
        }
    }
}
