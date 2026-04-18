package com.arelore.server.core.registration.service;

import com.arelore.server.core.registration.dto.MobileRegisterApplyRequest;
import com.arelore.server.core.registration.dto.MobileRegisterVerifyRequest;
import com.arelore.server.core.registration.dto.MobileResetPasswordApplyRequest;
import com.arelore.server.core.registration.dto.MobileResetPasswordConfirmRequest;

public interface UserRegistrationService {
    void applyMobileRegister(MobileRegisterApplyRequest request, String clientIp);

    String verifyMobileRegister(MobileRegisterVerifyRequest request, String clientIp);

    void applyMobileResetPassword(MobileResetPasswordApplyRequest request, String clientIp);

    void confirmMobileResetPassword(MobileResetPasswordConfirmRequest request, String clientIp);
}

