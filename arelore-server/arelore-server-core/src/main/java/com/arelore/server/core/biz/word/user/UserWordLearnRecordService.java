package com.arelore.server.core.biz.word.user;

import com.arelore.server.core.biz.word.user.dto.UserWordLearnRecordRequest;
import com.arelore.server.core.biz.word.user.dto.UserWordLearnRecordResponse;
import com.arelore.server.core.biz.word.user.dto.UserWordStudyAnswerRequest;
import com.arelore.server.core.biz.word.user.dto.UserWordStudyAnswerResponse;
import com.arelore.server.core.biz.word.user.dto.UserWordStudySessionRequest;
import com.arelore.server.core.biz.word.user.dto.UserWordStudySessionResponse;
import com.arelore.server.core.service.BaseService;

public interface UserWordLearnRecordService extends BaseService<UserWordLearnRecordRequest, UserWordLearnRecordResponse> {
    UserWordStudySessionResponse createSession(UserWordStudySessionRequest request);

    UserWordStudyAnswerResponse answer(UserWordStudyAnswerRequest request);
}
