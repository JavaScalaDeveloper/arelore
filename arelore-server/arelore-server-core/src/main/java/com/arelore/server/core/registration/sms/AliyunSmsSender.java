package com.arelore.server.core.registration.sms;

import com.aliyun.auth.credentials.provider.DefaultCredentialProvider;
import com.aliyun.auth.credentials.provider.ICredentialProvider;
import com.aliyun.auth.credentials.provider.StaticCredentialProvider;
import com.aliyun.auth.credentials.Credential;
import com.aliyun.sdk.service.dypnsapi20170525.AsyncClient;
import com.aliyun.sdk.service.dypnsapi20170525.models.SendSmsVerifyCodeRequest;
import com.aliyun.sdk.service.dypnsapi20170525.models.SendSmsVerifyCodeResponse;
import com.alibaba.fastjson2.JSON;
import darabonba.core.client.ClientOverrideConfiguration;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@ConditionalOnProperty(prefix = "aliyun.sms", name = "enabled", havingValue = "true")
public class AliyunSmsSender implements SmsSender, DisposableBean {
    private volatile AsyncClient client;
    private final String region;
    private final String endpoint;
    private final String accessKeyId;
    private final String accessKeySecret;
    private final String signName;
    private final String templateCode;
    private final int validMinutes;

    public AliyunSmsSender(
        @Value("${aliyun.sms.region:cn-hangzhou}") String region,
        @Value("${aliyun.sms.endpoint:dypnsapi.aliyuncs.com}") String endpoint,
        @Value("${aliyun.sms.access-key-id:}") String accessKeyId,
        @Value("${aliyun.sms.access-key-secret:}") String accessKeySecret,
        @Value("${aliyun.sms.sign-name:}") String signName,
        @Value("${aliyun.sms.template-code:}") String templateCode,
        @Value("${aliyun.sms.valid-minutes:5}") int validMinutes
    ) {
        this.region = region;
        this.endpoint = endpoint;
        this.accessKeyId = accessKeyId;
        this.accessKeySecret = accessKeySecret;
        this.signName = signName;
        this.templateCode = templateCode;
        this.validMinutes = validMinutes;
    }

    @Override
    public void sendVerifyCode(String phoneNumber, String code, int validMinutes) {
        if (signName == null || signName.isBlank() || templateCode == null || templateCode.isBlank()) {
            throw new IllegalStateException("短信配置不完整（sign-name/template-code）");
        }

        SendSmsVerifyCodeRequest req = SendSmsVerifyCodeRequest.builder()
            .signName(signName)
            .templateCode(templateCode)
            .phoneNumber(phoneNumber)
            .templateParam("{\"code\":\"" + code + "\",\"min\":\"" + validMinutes + "\"}")
            .build();

        CompletableFuture<SendSmsVerifyCodeResponse> future = getClient().sendSmsVerifyCode(req);
        try {
            SendSmsVerifyCodeResponse resp = future.get();
            // 成功与否以阿里云返回为准，这里仅在异常时抛出；保留结果用于排查
            String json = JSON.toJSONString(resp);
            if (json == null || json.isBlank()) {
                throw new IllegalStateException("短信发送失败：空响应");
            }
        } catch (Exception e) {
            throw new IllegalStateException("短信发送失败：" + e.getMessage(), e);
        }
    }

    public int getDefaultValidMinutes() {
        return validMinutes;
    }

    @Override
    public void destroy() throws Exception {
        AsyncClient c = client;
        if (c != null) {
            c.close();
        }
    }

    private AsyncClient getClient() {
        if (client == null) {
            synchronized (this) {
                if (client == null) {
                    ICredentialProvider provider;
                    if (accessKeyId != null && !accessKeyId.isBlank() && accessKeySecret != null && !accessKeySecret.isBlank()) {
                        provider = StaticCredentialProvider.create(
                            Credential.builder()
                                .accessKeyId(accessKeyId)
                                .accessKeySecret(accessKeySecret)
                                .build()
                        );
                    } else {
                        provider = DefaultCredentialProvider.builder().build();
                    }
                    client = AsyncClient.builder()
                        .region(region)
                        .credentialsProvider(provider)
                        .overrideConfiguration(ClientOverrideConfiguration.create().setEndpointOverride(endpoint))
                        .build();
                }
            }
        }
        return client;
    }
}

