package com.arelore.server.core.biz.prompt.user.dto;

import com.arelore.server.core.biz.prompt.user.entity.UserAiPromptPref;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserAiPromptPrefResponse extends UserAiPromptPref {
    /** 用户业务 ID 字符串，对应 userId */
    private String idStr;

    /** 字号倍率，来自 ext_json.fontScale */
    private BigDecimal fontScale;

    /** 列表基准 sp，来自 ext_json.fontSizeSp */
    private Integer fontSizeSp;

    /** 复制字段列表（顺序即拼接顺序），来自 ext_json.copyFields */
    private List<String> copyFields;

    /** 字段分隔符，来自 ext_json.copySep */
    private String copySep;

    /** 可选客户端版本，来自 ext_json.clientVersion */
    private String clientVersion;
}
