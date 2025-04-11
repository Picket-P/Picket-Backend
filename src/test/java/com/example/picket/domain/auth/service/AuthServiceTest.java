package com.example.picket.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.example.picket.common.dto.AuthUser;
import com.example.picket.common.enums.Gender;
import com.example.picket.common.enums.UserRole;
import com.example.picket.common.exception.CustomException;
import com.example.picket.config.PasswordEncoder;
import com.example.picket.domain.user.entity.User;
import com.example.picket.domain.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

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
            String alreadyExistEmail = "test@example.com";
            String password = "!Password1234";
            String nickname = "nickname1";
            LocalDate birth = LocalDate.of(2000, 12, 12);
            Gender gender = Gender.MALE;
            UserRole userRole = UserRole.USER;
            given(userRepository.existsByEmail(any())).willReturn(true);

            // when & then
            assertThatThrownBy(() -> authService.signup(alreadyExistEmail, password, nickname, birth, gender, userRole))
                    .isInstanceOf(CustomException.class)
                    .hasMessage("이미 가입되어있는 이메일 입니다.");
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
            given(userRepository.existsByEmail(any())).willReturn(false);

            // when
            authService.signup(email, password, nickname, birth, gender, userRole);

            // then
            verify(userRepository, times(1)).existsByEmail(email);
            verify(passwordEncoder, times(1)).encode(password);
            verify(userRepository, times(1)).save(any(User.class));
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
            User user = User.toEntity(email, notMatchPassword, userRole, null, nickname, birth, gender);
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
            User user = User.toEntity(email, password, userRole, null, nickname, birth, gender);
            given(userRepository.findByEmail(any())).willReturn(Optional.of(user));
            given(passwordEncoder.matches(any(), any())).willReturn(true);

            // when
            User signinUser = authService.signin(session, response, email, password);
            // then
            ArgumentCaptor<AuthUser> authUserCaptor = ArgumentCaptor.forClass(AuthUser.class);
            verify(session, times(1)).setAttribute(eq("authUser"), authUserCaptor.capture());

            AuthUser capturedAuthUser = authUserCaptor.getValue();
            assertEquals(user.getId(), capturedAuthUser.getId());
            assertEquals(user.getUserRole(), capturedAuthUser.getUserRole());

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
            // given

            // when
            authService.signout(session, response);
            // then
            verify(session, times(1)).invalidate();
        }
    }
}