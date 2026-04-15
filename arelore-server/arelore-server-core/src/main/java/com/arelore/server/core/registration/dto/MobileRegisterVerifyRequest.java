package com.arelore.server.core.registration.dto;

import lombok.Data;

@Data
public class MobileRegisterVerifyRequest {
    private String mobile;
    private String verifyCode;
}

