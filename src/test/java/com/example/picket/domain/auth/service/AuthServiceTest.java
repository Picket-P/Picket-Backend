package com.example.picket.domain.auth.service;

import com.example.picket.common.dto.AuthUser;
import com.example.picket.common.enums.Gender;
import com.example.picket.common.enums.UserRole;
import com.example.picket.common.exception.CustomException;
import com.example.picket.config.PasswordEncoder;
import com.example.picket.domain.email.service.EmailService;
import com.example.picket.domain.user.entity.User;
import com.example.picket.domain.user.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private HashOperations<String, Object, Object> hashOperations;

    @Mock
    private HttpSession session;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private AuthService authService;

    @Nested
    class 회원_가입_테스트 {

        @Test
        void 회원가입_시_존재하는_이메일로_회원가입_시도_할_경우_실패() {
            // given
            String email = "test@example.com";
            String password = "!Password1234";
            String nickname = "nickname1";
            LocalDate birth = LocalDate.of(2000, 12, 12);
            Gender gender = Gender.MALE;
            UserRole userRole = UserRole.USER;
            given(userRepository.existsByEmail(email)).willReturn(true);

            // when & then
            assertThatThrownBy(() -> authService.registerUserInfo(email, password, nickname, birth, gender, userRole))
                    .isInstanceOf(CustomException.class)
                    .hasMessage("이미 가입되어있는 이메일입니다.");
        }

        @Test
        void 회원_가입_성공() {
            // given
            String email = "test@example.com";
            String password = "!Password1234";
            String nickname = "nickname1";
            LocalDate birth = LocalDate.of(2000, 12, 12);
            Gender gender = Gender.MALE;
            UserRole userRole = UserRole.USER;
            String encodedPassword = "encodedPassword";

            given(userRepository.existsByEmail(email)).willReturn(false);
            given(passwordEncoder.encode(password)).willReturn(encodedPassword);
            given(redisTemplate.opsForHash()).willReturn(hashOperations);
            given(redisTemplate.opsForValue()).willReturn(valueOperations);

            // when
            String tempUserId = authService.registerUserInfo(email, password, nickname, birth, gender, userRole);

            // then
            verify(userRepository).existsByEmail(email);
            verify(passwordEncoder).encode(password);
            verify(hashOperations, times(6)).put(anyString(), any(), any());
            verify(redisTemplate).expire(anyString(), eq(30L), eq(TimeUnit.MINUTES));
            verify(valueOperations).set(anyString(), anyString(), eq(30L), eq(TimeUnit.MINUTES));
            verify(emailService).sendVerificationEmail(email);
            assertThat(tempUserId).isNotNull();
        }
    }

    @Nested
    class 이메일_인증_코드_검증_테스트 {

        @Test
        void 인증_코드_검증_실패() {
            // given
            String email = "test@example.com";
            String code = "123456";
            given(emailService.verifyCode(email, code)).willReturn(false);

            // when
            boolean result = authService.verifyCodeAndCompleteSignup(email, code);

            // then
            assertThat(result).isFalse();
            verify(emailService).verifyCode(email, code);
            verifyNoMoreInteractions(redisTemplate);
        }

        @Test
        void 인증_코드_검증_성공_임시_사용자_ID_없음() {
            // given
            String email = "test@example.com";
            String code = "123456";
            given(emailService.verifyCode(email, code)).willReturn(true);
            given(redisTemplate.opsForValue()).willReturn(valueOperations);
            given(valueOperations.get("email2tempId:" + email)).willReturn(null);

            // when & then
            assertThatThrownBy(() -> authService.verifyCodeAndCompleteSignup(email, code))
                    .isInstanceOf(CustomException.class)
                    .hasMessage("회원가입 정보가 만료되었습니다. 다시 시도해주세요.");
        }

        @Test
        void 인증_코드_검증_성공_이메일_불일치() {
            // given
            String email = "test@example.com";
            String code = "123456";
            String tempUserId = "temp-user-id";
            String tempUserKey = "tempUser:" + tempUserId;

            given(emailService.verifyCode(email, code)).willReturn(true);
            given(redisTemplate.opsForValue()).willReturn(valueOperations);
            given(valueOperations.get("email2tempId:" + email)).willReturn(tempUserId);
            given(redisTemplate.opsForHash()).willReturn(hashOperations);

            Map<Object, Object> userDataMap = new HashMap<>();
            userDataMap.put("email", "different@example.com");
            given(hashOperations.entries(tempUserKey)).willReturn(userDataMap);

            // when & then
            assertThatThrownBy(() -> authService.verifyCodeAndCompleteSignup(email, code))
                    .isInstanceOf(CustomException.class)
                    .hasMessage("회원정보가 일치하지 않습니다.");
        }

        @Test
        void 인증_코드_검증_및_회원가입_완료_성공() {
            // given
            String email = "test@example.com";
            String code = "123456";
            String tempUserId = "temp-user-id";
            String tempUserKey = "tempUser:" + tempUserId;
            String password = "encodedPassword";
            String nickname = "nickname1";
            LocalDate birth = LocalDate.of(2000, 12, 12);
            Gender gender = Gender.MALE;
            UserRole userRole = UserRole.USER;

            given(emailService.verifyCode(email, code)).willReturn(true);
            given(redisTemplate.opsForValue()).willReturn(valueOperations);
            given(valueOperations.get("email2tempId:" + email)).willReturn(tempUserId);
            given(redisTemplate.opsForHash()).willReturn(hashOperations);

            Map<Object, Object> userDataMap = new HashMap<>();
            userDataMap.put("email", email);
            userDataMap.put("password", password);
            userDataMap.put("nickname", nickname);
            userDataMap.put("birth", birth.toString());
            userDataMap.put("gender", gender.toString());
            userDataMap.put("userRole", userRole.toString());
            given(hashOperations.entries(tempUserKey)).willReturn(userDataMap);

            // when
            boolean result = authService.verifyCodeAndCompleteSignup(email, code);

            // then
            assertTrue(result);
            verify(userRepository).save(any(User.class));
            verify(redisTemplate, times(4)).delete(anyString());
        }
    }

    @Nested
    class 로그인_테스트 {

        @Test
        void 로그인_시도_시_존재하지_않는_이메일로_로그인_시도_할_경우_실패() {
            // given
            String notExistEmail = "test@example.com";
            String password = "!Password1234";
            given(userRepository.findByEmail(any())).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> authService.signin(session, response, notExistEmail, password))
                    .isInstanceOf(CustomException.class)
                    .hasMessage("해당 유저를 찾을 수 없습니다.");
        }

        @Test
        void 로그인_시도_시_일치하지_않는_비밀번호로_로그인_시도_할_경우_실패() {
            // given
            String email = "test@example.com";
            String notMatchPassword = "!Password1234";
            String nickname = "nickname1";
            LocalDate birth = LocalDate.of(2000, 12, 12);
            Gender gender = Gender.MALE;
            UserRole userRole = UserRole.USER;
            User user = User.create(email, notMatchPassword, userRole, null, nickname, birth, gender);
            given(userRepository.findByEmail(any())).willReturn(Optional.of(user));
            given(passwordEncoder.matches(any(), any())).willReturn(false);
            // when & then
            assertThatThrownBy(() -> authService.signin(session, response, email, notMatchPassword))
                    .isInstanceOf(CustomException.class)
                    .hasMessage("비밀번호가 일치하지 않습니다.");

        }

        @Test
        void 로그인_성공() {
            // given
            String email = "test@example.com";
            String password = "!Password1234";
            String nickname = "nickname1";
            LocalDate birth = LocalDate.of(2000, 12, 12);
            Gender gender = Gender.MALE;
            UserRole userRole = UserRole.USER;
            User user = User.create(email, password, userRole, null, nickname, birth, gender);
            given(userRepository.findByEmail(any())).willReturn(Optional.of(user));
            given(passwordEncoder.matches(any(), any())).willReturn(true);
            given(session.getId()).willReturn("session-id");

            // when
            User signinUser = authService.signin(session, response, email, password);
            // then
            ArgumentCaptor<AuthUser> authUserCaptor = ArgumentCaptor.forClass(AuthUser.class);
            verify(session, times(1)).setAttribute(eq("authUser"), authUserCaptor.capture());

            AuthUser capturedAuthUser = authUserCaptor.getValue();
            assertEquals(user.getId(), capturedAuthUser.getId());
            assertEquals(user.getUserRole(), capturedAuthUser.getUserRole());

            ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);
            verify(response).addCookie(cookieCaptor.capture());
            Cookie cookie = cookieCaptor.getValue();
            assertEquals("JSESSIONID", cookie.getName());
            assertEquals("session-id", cookie.getValue());
            assertEquals(24 * 60 * 60, cookie.getMaxAge());
            assertTrue(cookie.isHttpOnly());

            verify(userRepository, times(1)).findByEmail(any());
            verify(passwordEncoder, times(1)).matches(any(), any());

            assertThat(signinUser.getEmail()).isEqualTo(email);
            assertThat(signinUser.getNickname()).isEqualTo(nickname);
            assertThat(signinUser.getGender()).isEqualTo(gender);
            assertThat(signinUser.getUserRole()).isEqualTo(userRole);
        }
    }

    @Nested
    class 로그아웃_테스트 {
        @Test
        void 로그아웃_성공() {
            // when
            authService.signout(session, response);

            // then
            ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);
            verify(response).addCookie(cookieCaptor.capture());
            Cookie cookie = cookieCaptor.getValue();
            assertEquals("JSESSIONID", cookie.getName());
            assertThat(cookie.getValue()).isNull();
            assertEquals(0, cookie.getMaxAge());

            verify(session).invalidate();
        }
    }
}