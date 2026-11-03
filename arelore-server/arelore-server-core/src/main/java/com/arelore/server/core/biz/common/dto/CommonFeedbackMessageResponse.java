package com.arelore.server.core.biz.common.dto;

import com.arelore.server.core.registration.entity.CommonFeedbackMessage;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class CommonFeedbackMessageResponse extends CommonFeedbackMessage {
    /** 对应 senderUserId */
    private String idStr;
    /** 从 ext_info 解析出的附件列表 */
    private List<FeedbackAttachmentItem> files;
    private String clientIp;
}
