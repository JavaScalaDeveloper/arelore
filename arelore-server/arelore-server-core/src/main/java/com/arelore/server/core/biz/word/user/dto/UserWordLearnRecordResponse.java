package com.arelore.server.core.biz.word.user.dto;

import com.arelore.server.core.biz.word.user.entity.UserWordLearnRecord;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserWordLearnRecordResponse extends UserWordLearnRecord {
    /** 用户业务 ID 字符串（对应 userId），避免前端精度丢失 */
    private String idStr;
}
