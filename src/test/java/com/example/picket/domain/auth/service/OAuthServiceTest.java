package com.example.picket.domain.auth.service;

import com.example.picket.common.dto.AuthUser;
import com.example.picket.common.dto.OAuthUser;
import com.example.picket.common.enums.Gender;
import com.example.picket.common.enums.OAuth;
import com.example.picket.common.enums.UserRole;
import com.example.picket.domain.auth.client.GoogleOauthClient;
import com.example.picket.domain.user.entity.User;
import com.example.picket.domain.user.repository.UserRepository;
import com.example.picket.domain.user.service.UserQueryService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OAuthServiceTest {
    @InjectMocks
    private OAuthService oAuthService;

    @Mock
    private GoogleOauthClient googleOauthClient;

    @Mock
    private UserQueryService userQueryService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private HttpSession session;

    @Test
    void getOrCreateUser_기존유저_조회성공_세션저장_정상작동() {
        // given
        String code = "google-auth-code";
        String accessToken = "access-token";
        String email = "user@example.com";
        String oAuthId = "google-12345";
        UserRole role = UserRole.USER;

        OAuthUser oauthUser = OAuthUser.toEntity(oAuthId, email);
        User user = User.toOAuthEntity(email, role, oAuthId, OAuth.GOOGLE);
        ReflectionTestUtils.setField(user, "id", 1L);

        given(googleOauthClient.getAccessToken(anyString(), any(UserRole.class))).willReturn(accessToken);
        given(googleOauthClient.getUser(anyString())).willReturn(oauthUser);
        given(userQueryService.getUserByEmail(anyString())).willReturn(Optional.of(user));

        // when
        User result = oAuthService.getOrCreateUser(session, code, role);

        // then
        assertEquals(result, user);
        verify(session).setAttribute(eq("authUser"), any(AuthUser.class));
    }

    @Test
    void getOrCreateUser_신규유저_저장_세션저장_정상작동() {
        // given
        String code = "google-auth-code";
        String accessToken = "access-token";
        String email = "newuser@example.com";
        String oAuthId = "new-google-54321";
        UserRole role = UserRole.USER;

        OAuthUser oauthUser = OAuthUser.toEntity(oAuthId, email);
        User newUser = User.toOAuthEntity(email, role, oAuthId, OAuth.GOOGLE);
        ReflectionTestUtils.setField(newUser, "id", 100L);

        given(googleOauthClient.getAccessToken(anyString(), any(UserRole.class))).willReturn(accessToken);
        given(googleOauthClient.getUser(anyString())).willReturn(oauthUser);
        given(userQueryService.getUserByEmail(anyString())).willReturn(Optional.empty());
        given(userRepository.save(any(User.class))).willReturn(newUser);

        // when
        User result = oAuthService.getOrCreateUser(session, code, role);

        // then
        assertEquals(result.getEmail(), newUser.getEmail());
        verify(session).setAttribute(eq("authUser"), any(AuthUser.class));
    }

    @Test
    void signup_신규회원인_경우_정보추가() {
        // given
        Long userId = 1L;
        String oAuthId = "new-google-54321";
        String nickname = "닉네임";
        LocalDate birth = LocalDate.of(2000, 1, 1);
        Gender gender = Gender.FEMALE;

        User newUser = User.toOAuthEntity("user@example.com" , UserRole.USER ,oAuthId ,OAuth.GOOGLE);
        ReflectionTestUtils.setField(newUser, "id", userId);
        given(userQueryService.getUser(anyLong())).willReturn(newUser);

        // when
        oAuthService.signup(userId, nickname, birth, gender);

        // then
        assertEquals(newUser.getNickname(), nickname);
        assertEquals(newUser.getBirth(), birth);
        assertEquals(newUser.getGender(), gender);
    }
}