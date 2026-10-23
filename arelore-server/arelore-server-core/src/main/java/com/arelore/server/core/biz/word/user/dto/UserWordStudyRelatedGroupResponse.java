package com.arelore.server.core.biz.word.user.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class UserWordStudyRelatedGroupResponse {
    private String pos;
    private List<UserWordStudyRelatedItemResponse> words = new ArrayList<>();
}
