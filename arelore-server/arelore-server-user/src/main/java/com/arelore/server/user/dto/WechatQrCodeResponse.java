package com.arelore.server.user.dto;

import lombok.Data;

/**
 * 获取微信扫码二维码响应
 */
@Data
public class WechatQrCodeResponse {
    
    /**
     * 二维码 URL
     */
    private String qrCodeUrl;
    
    /**
     * 场景 ID（用于轮询检查）
     */
    private String sceneId;
    
    /**
     * 二维码过期时间（秒）
     */
    private Integer expireSeconds;
}
