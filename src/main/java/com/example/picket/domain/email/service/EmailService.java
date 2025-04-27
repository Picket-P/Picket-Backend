package com.example.picket.domain.email.service;

import com.example.picket.common.exception.CustomException;
import com.example.picket.domain.email.entity.EmailInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;
import software.amazon.awssdk.services.ses.model.SesException;

import java.security.SecureRandom;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Service
@RequiredArgsConstructor
public class EmailService {

    @Value("${aws.ses.send-mail-from}")
    private String sender;
    private final SesClient sesClient;
    private final TemplateEngine templateEngine;
    private final StringRedisTemplate redisTemplate;

    private String generateVerificationCode() {
        SecureRandom random = new SecureRandom();
        int code = 100000 + random.nextInt(900000); // 100000 ~ 999999
        return String.valueOf(code);
    }

    // 인증 코드 메일 발송
    public String sendVerificationEmail(String recipientEmail) {
        String verificationCode = generateVerificationCode();

        // Redis에 인증 코드 저장 (5분 TTL)
        redisTemplate.opsForValue().set(
                "verification:" + recipientEmail,
                verificationCode,
                5,
                TimeUnit.MINUTES
        );

        Context context = new Context();
        context.setVariable("verificationCode", verificationCode);

        String content = templateEngine.process("verification-email", context);

        EmailInfo emailInfo = new EmailInfo(
                sender,
                Collections.singletonList(recipientEmail),
                "회원가입 인증 코드",
                content
        );

        try {
            SendEmailRequest request = emailInfo.toSendEmailRequest();
            sesClient.sendEmail(request);
            return verificationCode;
        } catch (SesException e) {
            throw new CustomException(INTERNAL_SERVER_ERROR, "이메일 발송에 실패했습니다. 잠시 후 다시 시도해주세요.");
        }
    }

    // 인증 코드 검증
    public boolean verifyCode(String email, String code) {
        String storedCode = redisTemplate.opsForValue().get("verification:" + email);

        if (storedCode != null && storedCode.equals(code)) {
            redisTemplate.opsForValue().set("verified:" + email, "true", 30, TimeUnit.MINUTES);
            return true;
        }

        return false;
    }
}