package com.arelore.server.core.registration.service;

import com.arelore.server.core.registration.dto.MobileRegisterApplyRequest;
import com.arelore.server.core.registration.dto.MobileRegisterVerifyRequest;

public interface UserRegistrationService {
    void applyMobileRegister(MobileRegisterApplyRequest request, String clientIp);

    String verifyMobileRegister(MobileRegisterVerifyRequest request, String clientIp);
}

