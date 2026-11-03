package com.arelore.server.core.biz.common;

import com.arelore.server.core.biz.common.dto.CommonFeedbackMessageRequest;
import com.arelore.server.core.biz.common.dto.CommonFeedbackMessageResponse;
import com.arelore.server.core.biz.common.support.FeedbackExtSupport;
import com.arelore.server.core.registration.entity.CommonFeedbackMessage;
import com.arelore.server.core.registration.mapper.CommonFeedbackMessageMapper;
import com.arelore.server.core.service.BaseServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class CommonFeedbackMessageServiceImpl
    extends BaseServiceImpl<CommonFeedbackMessageRequest, CommonFeedbackMessageResponse, CommonFeedbackMessage>
    implements CommonFeedbackMessageService {

    private final CommonFeedbackMessageMapper mapper;

    public CommonFeedbackMessageServiceImpl(CommonFeedbackMessageMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    protected CommonFeedbackMessageMapper mapper() {
        return mapper;
    }

    @Override
    protected Class<CommonFeedbackMessageResponse> responseClass() {
        return CommonFeedbackMessageResponse.class;
    }

    @Override
    protected LambdaQueryWrapper<CommonFeedbackMessage> buildWrapper(CommonFeedbackMessageRequest request) {
        LambdaQueryWrapper<CommonFeedbackMessage> wrapper = new LambdaQueryWrapper<>();
        if (request == null) {
            wrapper.orderByAsc(CommonFeedbackMessage::getId);
            return wrapper;
        }
        if (request.getId() != null) {
            wrapper.eq(CommonFeedbackMessage::getId, request.getId());
        }
        if (request.getTicketId() != null) {
            wrapper.eq(CommonFeedbackMessage::getTicketId, request.getTicketId());
        }
        if (request.getSenderType() != null && !request.getSenderType().isBlank()) {
            wrapper.eq(CommonFeedbackMessage::getSenderType, request.getSenderType().trim());
        }
        wrapper.orderByAsc(CommonFeedbackMessage::getId);
        return wrapper;
    }

    @Override
    protected CommonFeedbackMessageResponse toResponse(CommonFeedbackMessage entity) {
        CommonFeedbackMessageResponse res = super.toResponse(entity);
        if (res == null) {
            return null;
        }
        if (res.getSenderUserId() != null) {
            res.setIdStr(res.getSenderUserId().toPlainString());
        }
        res.setFiles(FeedbackExtSupport.parseFiles(res.getExtInfo()));
        res.setClientIp(FeedbackExtSupport.parseClientIp(res.getExtInfo()));
        return res;
    }

    @Override
    public List<CommonFeedbackMessageResponse> listByTicketId(Long ticketId) {
        if (ticketId == null) {
            return Collections.emptyList();
        }
        CommonFeedbackMessageRequest req = new CommonFeedbackMessageRequest();
        req.setTicketId(ticketId);
        return list(req);
    }
}
