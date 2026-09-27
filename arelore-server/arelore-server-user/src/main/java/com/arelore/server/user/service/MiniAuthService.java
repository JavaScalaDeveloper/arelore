package com.arelore.server.user.service;

import com.arelore.server.core.biz.user.dto.MiniManualLoginRequest;
import com.arelore.server.core.biz.user.dto.AuthLoginResponse;

public interface MiniAuthService {
    AuthLoginResponse manualLogin(MiniManualLoginRequest request, String clientIp);
}

