package com.arelore.server.core.biz.user.entity;

import lombok.Data;

/**
 * 二维码场景信息
 */
@Data
public class QrCodeScene {
    
    /**
     * 场景 ID
     */
    private String sceneId;
    
    /**
     * 状态：WAIT(等待扫码), SCANED(已扫码), CONFIRMED(已确认登录), EXPIRED(已过期)
     */
    private String status;
    
    /**
     * 创建时间
     */
    private Long createTime;
    
    /**
     * 过期时间
     */
    private Long expireTime;
}
