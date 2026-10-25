package com.arelore.server.core.biz.prompt.user.dto;

import com.arelore.server.core.biz.prompt.user.entity.UserAiPrompt;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserAiPromptResponse extends UserAiPrompt {
    /** userId 字符串，前端展示与回传用 */
    private String idStr;
    /** config_tags 解析列表 */
    private List<String> configTagList;
    /** manual_tags 解析列表 */
    private List<String> manualTagList;
    /** 配置+人工合并列表，便于列表展示/兼容旧客户端 */
    private List<String> tagList;
    /** ext_info.summary */
    private String summary;
}
