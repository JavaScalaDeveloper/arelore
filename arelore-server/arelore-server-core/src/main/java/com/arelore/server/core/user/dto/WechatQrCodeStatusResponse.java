package com.arelore.server.core.user.dto;

import lombok.Data;

/**
 * 检查微信扫码状态响应
 */
@Data
public class WechatQrCodeStatusResponse {
    
    /**
     * 扫码状态
     * WAIT: 等待扫码
     * SCANED: 已扫码
     * CONFIRMED: 已确认登录
     * EXPIRED: 已过期
     */
    private String status;
    
    /**
     * 用户信息（扫码成功后返回）
     */
    private WechatUserInfo userInfo;
    
    /**
     * 授权码（扫码成功后返回）
     */
    private String code;
    
    /**
     * 微信用户信息
     */
    @Data
    public static class WechatUserInfo {
        /**
         * 微信 openid
         */
        private String openid;
        
        /**
         * 昵称
         */
        private String nickname;
        
        /**
         * 头像
         */
        private String avatar;
    }
}
