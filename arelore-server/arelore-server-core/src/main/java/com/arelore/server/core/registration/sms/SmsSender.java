package com.arelore.server.core.registration.sms;

public interface SmsSender {
    void sendVerifyCode(String phoneNumber, String code, int validMinutes);
}

