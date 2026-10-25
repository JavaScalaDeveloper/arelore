package com.arelore.server.core.biz.prompt.user.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserAiPromptRequest extends UserAiPromptResponse {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    /** 标题模糊搜索 */
    private String titleKeyword;
    /** 标签过滤：命中 config_tags 或 manual_tags 中该名称（走 JSON 多值索引） */
    private String tagName;
    /**
     * 多标签 OR 过滤：命中任一名称即可，如 ["b","c"]。
     * 优先于 tagName；走 JSON 多值索引（MEMBER OF / JSON_CONTAINS）
     */
    private List<String> tagNames;
    /** 仅过滤配置标签 */
    private String configTagName;
    /** 仅过滤人工标签 */
    private String manualTagName;
    /**
     * true=导入模式：只写 config_tags，保留库内 manual_tags；
     * false/缺省=页面保存：两套标签都按入参写入
     */
    private Boolean importMode;
    /** 批量导入条目 */
    private List<UserAiPromptRequest> items;
}
