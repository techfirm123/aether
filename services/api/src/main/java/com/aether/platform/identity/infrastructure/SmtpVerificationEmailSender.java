package com.aether.platform.identity.infrastructure;

import com.aether.platform.identity.application.VerificationEmailSender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@Profile("prod")
public class SmtpVerificationEmailSender implements VerificationEmailSender {

    private final JavaMailSender mailSender;
    private final String fromAddress;
    private final Duration tokenTtl;

    public SmtpVerificationEmailSender(
            JavaMailSender mailSender,
            @Value("${aether.identity.email.from}") String fromAddress,
            @Value("${aether.identity.verification-token-ttl:PT24H}") Duration tokenTtl) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
        this.tokenTtl = tokenTtl;
    }

    @Override
    public void sendVerificationToken(String emailAddress, String displayName, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(emailAddress);
        message.setSubject("Verify your AETHER account");
        message.setText("""
                Hello %s,

                Your AETHER email verification token is:

                %s

                The token is valid for %d hours and can be used only once.
                If you did not request this account, you can ignore this email.
                """.formatted(displayName, token, tokenTtl.toHours()));

        mailSender.send(message);
    }
}