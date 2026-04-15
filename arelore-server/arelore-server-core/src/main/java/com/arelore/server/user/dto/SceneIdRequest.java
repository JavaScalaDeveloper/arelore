package com.arelore.server.user.dto;

import lombok.Data;

/**
 * 二维码轮询接口请求体。
 */
@Data
public class SceneIdRequest {
    /**
     * 二维码场景唯一标识。
     */
    private String sceneId;
}

