package com.arelore.server.core.biz.common.dto;

import com.arelore.server.core.registration.entity.CommonFeedbackTicket;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class CommonFeedbackTicketResponse extends CommonFeedbackTicket {
    /** 对应 userId，防前端精度丢失 */
    private String idStr;
    /** 对应 adminUserId */
    private String adminUserIdStr;
    /** 详情接口带回的会话消息 */
    private List<CommonFeedbackMessageResponse> messages;
}
