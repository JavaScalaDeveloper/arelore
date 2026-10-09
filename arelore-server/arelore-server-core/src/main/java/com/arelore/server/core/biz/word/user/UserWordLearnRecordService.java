package com.arelore.server.core.biz.word.user;

import com.arelore.server.core.biz.word.user.dto.UserWordCurrentBookResponse;
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

    /** 首页：校正今日新学/复习待办，并填充整书进度与剩余天数 */
    UserWordCurrentBookResponse enrichHome(UserWordCurrentBookResponse current);
}
