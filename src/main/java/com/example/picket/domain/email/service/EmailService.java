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
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Service
@RequiredArgsConstructor
public class EmailService {

    private static final String VERIFICATION_KEY_PREFIX = "verification:";
    private static final String VERIFIED_KEY_PREFIX = "verified:";
    private static final int VERIFICATION_EXPIRY_MINUTES = 5;
    private static final int VERIFIED_STATUS_EXPIRY_MINUTES = 30;

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

        // Redis에 인증 코드 저장
        saveVerificationCode(recipientEmail, verificationCode);

        // 메일 콘텐츠 생성
        String content = createEmailContent(verificationCode);

        // 메일 발송
        sendEmail(recipientEmail, "회원가입 인증 코드", content);

        return verificationCode;
    }

    // 인증 코드 검증
    public boolean verifyCode(String email, String code) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(VERIFICATION_KEY_PREFIX + email))
                .filter(storedCode -> storedCode.equals(code))
                .map(storedCode -> {
                    markEmailAsVerified(email);
                    return true;
                })
                .orElse(false);
    }

    // Redis에 인증 코드 저장
    private void saveVerificationCode(String email, String code) {
        redisTemplate.opsForValue().set(
                VERIFICATION_KEY_PREFIX + email,
                code,
                VERIFICATION_EXPIRY_MINUTES,
                TimeUnit.MINUTES
        );
    }

    // 이메일을 인증 완료로 표시
    private void markEmailAsVerified(String email) {
        redisTemplate.opsForValue().set(
                VERIFIED_KEY_PREFIX + email,
                "true",
                VERIFIED_STATUS_EXPIRY_MINUTES,
                TimeUnit.MINUTES
        );
    }

    // 이메일 콘텐츠 생성
    private String createEmailContent(String verificationCode) {
        Context context = new Context();
        context.setVariable("verificationCode", verificationCode);
        return templateEngine.process("verification-email", context);
    }

    // 실제 이메일 발송 처리
    private void sendEmail(String recipientEmail, String subject, String content) {
        EmailInfo emailInfo = EmailInfo.create(
                sender,
                Collections.singletonList(recipientEmail),
                subject,
                content
        );

        try {
            SendEmailRequest request = emailInfo.toSendEmailRequest();
            sesClient.sendEmail(request);
        } catch (SesException e) {
            throw new CustomException(INTERNAL_SERVER_ERROR, "이메일 발송에 실패했습니다. 잠시 후 다시 시도해주세요.");
        }
    }
}