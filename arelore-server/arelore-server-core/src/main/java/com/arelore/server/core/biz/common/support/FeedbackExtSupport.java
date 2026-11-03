package com.arelore.server.core.biz.common.support;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.arelore.server.core.biz.common.dto.FeedbackAttachmentItem;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 反馈消息 ext_info 约定：clientIp、files[{fileId,fileName,contentType}]。
 */
public final class FeedbackExtSupport {
    private FeedbackExtSupport() {
    }

    public static String buildMessageExt(String clientIp, List<FeedbackAttachmentItem> files) {
        JSONObject ext = new JSONObject();
        if (StringUtils.hasText(clientIp)) {
            ext.put("clientIp", clientIp.trim());
        }
        JSONArray arr = new JSONArray();
        if (files != null) {
            for (FeedbackAttachmentItem item : files) {
                if (item == null || item.getFileId() == null) {
                    continue;
                }
                JSONObject one = new JSONObject();
                one.put("fileId", item.getFileId());
                one.put("fileName", item.getFileName() == null ? "" : item.getFileName());
                one.put("contentType", item.getContentType() == null ? "" : item.getContentType());
                arr.add(one);
            }
        }
        if (!arr.isEmpty()) {
            ext.put("files", arr);
        }
        return ext.isEmpty() ? null : ext.toJSONString();
    }

    public static List<FeedbackAttachmentItem> parseFiles(String extInfo) {
        if (!StringUtils.hasText(extInfo)) {
            return Collections.emptyList();
        }
        try {
            JSONObject ext = JSON.parseObject(extInfo);
            if (ext == null) {
                return Collections.emptyList();
            }
            JSONArray arr = ext.getJSONArray("files");
            if (arr == null || arr.isEmpty()) {
                return Collections.emptyList();
            }
            List<FeedbackAttachmentItem> list = new ArrayList<>(arr.size());
            for (int i = 0; i < arr.size(); i++) {
                JSONObject one = arr.getJSONObject(i);
                if (one == null || one.getLong("fileId") == null) {
                    continue;
                }
                FeedbackAttachmentItem item = new FeedbackAttachmentItem();
                item.setFileId(one.getLong("fileId"));
                item.setFileName(one.getString("fileName"));
                item.setContentType(one.getString("contentType"));
                list.add(item);
            }
            return list;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public static String parseClientIp(String extInfo) {
        if (!StringUtils.hasText(extInfo)) {
            return null;
        }
        try {
            JSONObject ext = JSON.parseObject(extInfo);
            return ext == null ? null : ext.getString("clientIp");
        } catch (Exception e) {
            return null;
        }
    }
}
