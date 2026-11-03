package com.arelore.server.core.biz.common.dto;

import lombok.Data;

/**
 * 反馈消息附件项（写入 message.ext_info.files）。
 */
@Data
public class FeedbackAttachmentItem {
    private Long fileId;
    private String fileName;
    private String contentType;
}
