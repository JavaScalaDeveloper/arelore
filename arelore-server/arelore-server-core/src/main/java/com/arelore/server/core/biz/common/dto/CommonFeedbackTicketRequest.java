package com.arelore.server.core.biz.common.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class CommonFeedbackTicketRequest extends CommonFeedbackTicketResponse {
    private Integer pageNum = 1;
    private Integer pageSize = 10;

    /** 创建/回复时的正文 */
    private String contentText;
    /** 创建/回复时的附件 */
    private List<FeedbackAttachmentItem> files;
    /** 客户端 IP（服务端可覆盖） */
    private String clientIp;
    /** 用户侧按 idStr 查自己的工单（对应 userId） */
    private String userIdStr;
}
