package com.example.picket.domain.email.service;

import com.example.picket.common.exception.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.SendEmailRequest;
import software.amazon.awssdk.services.ses.model.SesException;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private SesClient sesClient;

    @Mock
    private TemplateEngine templateEngine;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private EmailService emailService;

    @BeforeEach
    void setUp() {
        emailService = new EmailService(sesClient, templateEngine, redisTemplate);
        ReflectionTestUtils.setField(emailService, "sender", "test@example.com");
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void 인증_코드_생성_시_생성된_코드는_6자리_숫자() throws Exception {
        // given
        Method method = EmailService.class.getDeclaredMethod("generateVerificationCode");
        method.setAccessible(true);

        // when
        String code = (String) method.invoke(emailService);

        // then
        assertThat(code).hasSize(6);
        assertThat(code).matches("\\d{6}");
    }

    @Test
    void 인증_코드_저장_성공() throws Exception {
        // given
        String email = "user@example.com";
        String code = "123456";
        Method method = EmailService.class.getDeclaredMethod("saveVerificationCode", String.class, String.class);
        method.setAccessible(true);

        // when
        method.invoke(emailService, email, code);

        // then
        verify(valueOperations).set(
                eq("verification:user@example.com"),
                eq("123456"),
                eq(5L),
                eq(TimeUnit.MINUTES)
        );
    }

    @Test
    void 이메일_인증_완료_표시_성공() throws Exception {
        // given
        String email = "user@example.com";
        Method method = EmailService.class.getDeclaredMethod("markEmailAsVerified", String.class);
        method.setAccessible(true);

        // when
        method.invoke(emailService, email);

        // then
        verify(valueOperations).set(
                eq("verified:user@example.com"),
                eq("true"),
                eq(30L),
                eq(TimeUnit.MINUTES)
        );
    }

    @Test
    void 이메일_발송_시_템플릿_처리_성공() throws Exception {
        // given
        String code = "123456";
        String processedContent = "<html>코드: 123456</html>";
        Method method = EmailService.class.getDeclaredMethod("createEmailContent", String.class);
        method.setAccessible(true);

        given(templateEngine.process(eq("verification-email"), any(Context.class))).willReturn(processedContent);

        // when
        String content = (String) method.invoke(emailService, code);

        // then
        verify(templateEngine).process(eq("verification-email"), any(Context.class));
        assertThat(content).isEqualTo(processedContent);
    }

    @Nested
    class 이메일_발송_테스트 {
        @Test
        void 이메일_발송_성공() throws Exception {
            // given
            String recipientEmail = "user@example.com";
            String subject = "테스트 제목";
            String content = "<html>테스트 내용</html>";
            Method method = EmailService.class.getDeclaredMethod("sendEmail", String.class, String.class, String.class);
            method.setAccessible(true);

            // when
            method.invoke(emailService, recipientEmail, subject, content);

            // then
            ArgumentCaptor<SendEmailRequest> requestCaptor = ArgumentCaptor.forClass(SendEmailRequest.class);
            verify(sesClient).sendEmail(requestCaptor.capture());

            SendEmailRequest capturedRequest = requestCaptor.getValue();
            assertThat(capturedRequest.source()).isEqualTo("test@example.com");
            assertThat(capturedRequest.destination().toAddresses()).contains("user@example.com");
            assertThat(capturedRequest.message().subject().data()).isEqualTo("테스트 제목");
            assertThat(capturedRequest.message().body().html().data()).isEqualTo("<html>테스트 내용</html>");
        }

        @Test
        void SES_예외_발생시_CustomException_반환() throws Exception {
            // given
            String recipientEmail = "user@example.com";
            String subject = "테스트 제목";
            String content = "<html>테스트 내용</html>";
            Method method = EmailService.class.getDeclaredMethod("sendEmail", String.class, String.class, String.class);
            method.setAccessible(true);

            doThrow(SesException.class).when(sesClient).sendEmail(any(SendEmailRequest.class));

            // when & then
            assertThatThrownBy(() -> method.invoke(emailService, recipientEmail, subject, content))
                    .hasCauseInstanceOf(CustomException.class);
        }

        @Test
        void 인증_메일_발송_성공() {
            // given
            String email = "user@example.com";
            String processedContent = "<html>코드: 123456</html>";

            given(templateEngine.process(eq("verification-email"), any(Context.class))).willReturn(processedContent);

            // when
            String resultCode = emailService.sendVerificationEmail(email);

            // then
            assertThat(resultCode).hasSize(6);
            assertThat(resultCode).matches("\\d{6}");

            verify(valueOperations).set(
                    argThat(key -> key.startsWith("verification:")),
                    eq(resultCode),
                    eq(5L),
                    eq(TimeUnit.MINUTES)
            );

            verify(sesClient).sendEmail(any(SendEmailRequest.class));
        }
    }

    @Nested
    class 인증코드_테스트 {
        @Test
        void 인증_코드_검증_성공() {
            // given
            String email = "user@example.com";
            String code = "123456";
            given(valueOperations.get("verification:user@example.com")).willReturn(code);

            // when
            boolean result = emailService.verifyCode(email, code);

            // then
            assertThat(result).isTrue();
            verify(valueOperations).set(
                    eq("verified:user@example.com"),
                    eq("true"),
                    eq(30L),
                    eq(TimeUnit.MINUTES)
            );
        }

        @Test
        void 저장된_코드_없음() {
            // given
            String email = "user@example.com";
            String code = "123456";
            given(valueOperations.get("verification:user@example.com")).willReturn(null);

            // when
            boolean result = emailService.verifyCode(email, code);

            // then
            assertThat(result).isFalse();
            verify(valueOperations, never()).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));
        }

        @Test
        void 코드_불일치() {
            // given
            String email = "user@example.com";
            String code = "123456";
            String storedCode = "654321";
            given(valueOperations.get("verification:user@example.com")).willReturn(storedCode);

            // when
            boolean result = emailService.verifyCode(email, code);

            // then
            assertThat(result).isFalse();
            verify(valueOperations, never()).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));
        }
    }
}