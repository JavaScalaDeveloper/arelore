package com.arelore.server.user.controller;

import com.arelore.server.core.common.result.Result;
import com.arelore.server.core.user.dto.TextMaskRequest;
import com.arelore.server.core.user.dto.TextMaskResponse;
import com.arelore.server.core.user.service.TextMaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 文本脱敏控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/user/text-mask")
public class TextMaskController {
    
    @Autowired
    private TextMaskService textMaskService;
    
    /**
     * 执行文本脱敏
     * 
     * POST /api/user/text-mask/mask
     */
    @PostMapping("/mask")
    public Result<TextMaskResponse> maskText(@RequestBody TextMaskRequest request) {
        log.info("接收到文本脱敏请求");
        
        try {
            TextMaskResponse response = textMaskService.maskText(request);
            return Result.success(response);
        } catch (IllegalArgumentException e) {
            log.error("参数错误：{}", e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("文本脱敏失败", e);
            return Result.error("文本脱敏失败：" + e.getMessage());
        }
    }
}
