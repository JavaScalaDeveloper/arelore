package com.arelore.server.core.user.service;

import com.arelore.server.core.user.dto.TextMaskRequest;
import com.arelore.server.core.user.dto.TextMaskResponse;

/**
 * 文本脱敏服务接口
 */
public interface TextMaskService {
    
    /**
     * 执行文本脱敏
     * 
     * @param request 脱敏请求
     * @return 脱敏响应
     */
    TextMaskResponse maskText(TextMaskRequest request);
}
