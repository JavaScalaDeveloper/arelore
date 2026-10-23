package com.arelore.server.core.biz.word.user.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class UserWordStudySynonymGroupResponse {
    private String pos;
    private String tran;
    private List<String> words = new ArrayList<>();
}
