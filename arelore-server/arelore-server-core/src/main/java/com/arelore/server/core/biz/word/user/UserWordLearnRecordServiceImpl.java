package com.arelore.server.core.biz.word.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.arelore.server.core.service.BaseServiceImpl;
import com.arelore.server.core.biz.word.user.dto.UserWordLearnRecordRequest;
import com.arelore.server.core.biz.word.user.dto.UserWordLearnRecordResponse;
import com.arelore.server.core.biz.word.user.entity.UserWordLearnRecord;
import com.arelore.server.core.biz.word.user.mapper.UserWordLearnRecordMapper;
import com.arelore.server.core.biz.word.user.UserWordLearnRecordService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class UserWordLearnRecordServiceImpl
    extends BaseServiceImpl<UserWordLearnRecordRequest, UserWordLearnRecordResponse, UserWordLearnRecord>
    implements UserWordLearnRecordService {
    private final UserWordLearnRecordMapper mapper;

    public UserWordLearnRecordServiceImpl(UserWordLearnRecordMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    protected UserWordLearnRecordMapper mapper() {
        return mapper;
    }

    @Override
    protected Class<UserWordLearnRecordResponse> responseClass() {
        return UserWordLearnRecordResponse.class;
    }

    @Override
    protected LambdaQueryWrapper<UserWordLearnRecord> buildWrapper(UserWordLearnRecordRequest request) {
        LambdaQueryWrapper<UserWordLearnRecord> wrapper = new LambdaQueryWrapper<>();
        if (request == null) {
            return wrapper;
        }
        if (request.getUserId() != null) {
            wrapper.eq(UserWordLearnRecord::getUserId, request.getUserId());
        }
        if (StringUtils.hasText(request.getBookCode())) {
            wrapper.eq(UserWordLearnRecord::getBookCode, request.getBookCode());
        }
        if (StringUtils.hasText(request.getWordCode())) {
            wrapper.eq(UserWordLearnRecord::getWordCode, request.getWordCode());
        }
        wrapper.orderByDesc(UserWordLearnRecord::getModifyTime);
        return wrapper;
    }
}
