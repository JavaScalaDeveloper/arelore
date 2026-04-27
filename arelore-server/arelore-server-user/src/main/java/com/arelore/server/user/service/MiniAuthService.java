package com.arelore.server.user.service;

import com.arelore.server.core.user.dto.MiniManualLoginRequest;
import com.arelore.server.core.user.dto.AuthLoginResponse;

public interface MiniAuthService {
    AuthLoginResponse manualLogin(MiniManualLoginRequest request, String clientIp);
}

