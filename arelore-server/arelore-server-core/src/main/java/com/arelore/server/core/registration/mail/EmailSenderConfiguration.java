package com.arelore.server.core.registration.mail;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * EmailSender 装配：优先 SMTP，否则 Noop。
 */
@Configuration
public class EmailSenderConfiguration {

    @Bean
    @Primary
    public EmailSender emailSender(
        ObjectProvider<SmtpEmailSender> smtpProvider,
        ObjectProvider<NoopEmailSender> noopProvider
    ) {
        try {
            SmtpEmailSender smtp = smtpProvider.getIfAvailable();
            if (smtp != null) {
                return smtp;
            }
        } catch (Exception ignored) {
            // SMTP bean 初始化失败时降级
        }
        NoopEmailSender noop = noopProvider.getIfAvailable();
        if (noop != null) {
            return noop;
        }
        return new NoopEmailSender();
    }
}
