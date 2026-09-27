package com.arelore.server.core.biz.user.dto;

import lombok.Data;

/**
 * 微信一键登录请求
 */
@Data
public class WechatQuickLoginRequest {
    
    /**
     * 微信授权码
     */
    private String code;
    
    /**
     * 微信用户信息
     */
    private WechatUserInfo userInfo;
    
    /**
     * 微信用户基本信息
     */
    @Data
    public static class WechatUserInfo {
        /**
         * 微信 openid
         */
        private String openid;
        
        /**
         * 微信昵称
         */
        private String nickname;
        
        /**
         * 头像 URL
         */
        private String avatar;
        
        /**
         * 性别 (1-男，2-女，0-未知)
         */
        private Integer gender;
        
        /**
         * 国家
         */
        private String country;
        
        /**
         * 省份
         */
        private String province;
        
        /**
         * 城市
         */
        private String city;
    }
}
