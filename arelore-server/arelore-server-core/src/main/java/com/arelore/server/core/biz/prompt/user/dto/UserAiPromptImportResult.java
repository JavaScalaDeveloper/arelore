package com.arelore.server.core.biz.prompt.user.dto;

import lombok.Data;

@Data
public class UserAiPromptImportResult {
    private int created;
    private int updated;
    private int skipped;
    private String message;
}
