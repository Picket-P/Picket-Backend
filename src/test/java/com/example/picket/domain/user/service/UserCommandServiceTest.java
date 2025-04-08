package com.example.picket.domain.user.service;

import com.example.picket.common.dto.AuthUser;
import com.example.picket.common.enums.UserRole;
import com.example.picket.common.exception.CustomException;
import com.example.picket.config.PasswordEncoder;
import com.example.picket.domain.user.dto.request.UpdatePasswordRequest;
import com.example.picket.domain.user.dto.request.UpdateUserRequest;
import com.example.picket.domain.user.dto.request.WithdrawUserRequest;
import com.example.picket.domain.user.entity.User;
import com.example.picket.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserCommandServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserCommandService userCommandService;

    @Test
    void 존재하지않는_사용자를_조회시_예외처리를_던진다() {
        // given
        Long userId = 1L;
        AuthUser authUser = AuthUser.toEntity(userId, UserRole.USER);
        UpdateUserRequest request = mock();

        BDDMockito.given(userRepository.findById(anyLong())).willReturn(Optional.empty());
        // when & then
        assertThrows(CustomException.class, () -> userCommandService.updateUser(authUser, request), "해당 유저를 찾을 수 없습니다."
        );
    }

    @Test
    void 유저정보_업데이트_시_입력한_비밀번호가_잘못되면_예외처리를_던진다() {
        // given
        Long userId = 1L;
        AuthUser authUser = AuthUser.toEntity(userId, UserRole.USER);
        UpdateUserRequest request = mock();
        User user = mock();
        BDDMockito.given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
        // when & then
        assertThrows(CustomException.class, () -> userCommandService.updateUser(authUser, request),
                "비밀번호가 일치하지 않습니다.");
    }

    @Test
    void 사용자가_프로필을_변경할_수_있다() {
        // given
        Long userId = 1L;
        AuthUser authUser = AuthUser.toEntity(userId, UserRole.USER);
        UpdateUserRequest request = new UpdateUserRequest();
        ReflectionTestUtils.setField(request, "password", "test");
        ReflectionTestUtils.setField(request, "nickname", "test");
        ReflectionTestUtils.setField(request, "profileUrl", "newProfileUrl");

        User user = mock(User.class);
        BDDMockito.given(user.getPassword()).willReturn("test");
        BDDMockito.given(userRepository.findById(userId)).willReturn(Optional.of(user));
        BDDMockito.given(passwordEncoder.matches("test", "test")).willReturn(true);

        // when
        User response = userCommandService.updateUser(authUser, request);

        // then
        assertThat(response).isNotNull();
        verify(user).update("test", "newProfileUrl");
    }

    @Test
    void 비밀번호_업데이트_시_존재하지_않는_사용자를_조회하면_예외처리를_던진다() {
        // given
        Long userId = 1L;
        AuthUser authUser = AuthUser.toEntity(userId, UserRole.USER);
        UpdatePasswordRequest request = mock();

        BDDMockito.given(userRepository.findById(anyLong())).willReturn(Optional.empty());
        // when & then
        assertThrows(CustomException.class, () -> userCommandService.updatePassword(authUser, request), "해당 유저를 찾을 수 없습니다."
        );
    }

    @Test
    void 비밀번호_업데이트_시_입력한_비밀번호가_잘못되면_예외처리를_던진다() {
        // given
        Long userId = 1L;
        AuthUser authUser = AuthUser.toEntity(userId, UserRole.USER);
        UpdatePasswordRequest request = mock();
        User user = mock();
        BDDMockito.given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
        // when & then
        assertThrows(CustomException.class, () -> userCommandService.updatePassword(authUser, request),
                "비밀번호가 일치하지 않습니다.");
    }

    @Test
    void 사용자의_비밀번호를_변경할_수_있다() {
        // given
        Long userId = 1L;
        AuthUser authUser = AuthUser.toEntity(userId, UserRole.USER);
        UpdatePasswordRequest request = new UpdatePasswordRequest();
        ReflectionTestUtils.setField(request, "password", "test");
        ReflectionTestUtils.setField(request, "newPassword", "test2");

        User user = mock(User.class);
        BDDMockito.given(user.getPassword()).willReturn("test");
        BDDMockito.given(userRepository.findById(userId)).willReturn(Optional.of(user));
        BDDMockito.given(passwordEncoder.matches("test", "test")).willReturn(true);
        BDDMockito.given(passwordEncoder.encode("test2")).willReturn("test2");

        // when
        userCommandService.updatePassword(authUser, request);

        // then
        verify(user).passwordUpdate("test2");
    }

    @Test
    void 유저탈퇴_시_존재하지_않는_사용자를_조회하면_예외처리를_던진다() {
        // given
        Long userId = 1L;
        AuthUser authUser = AuthUser.toEntity(userId, UserRole.USER);
        WithdrawUserRequest request = mock();

        BDDMockito.given(userRepository.findById(anyLong())).willReturn(Optional.empty());
        // when & then
        assertThrows(CustomException.class, () -> userCommandService.withdrawUserRequest(authUser, request), "해당 유저를 찾을 수 없습니다."
        );
    }

    @Test
    void 유저탈퇴_시_입력한_비밀번호가_잘못되면_예외처리를_던진다() {
        // given
        Long userId = 1L;
        AuthUser authUser = AuthUser.toEntity(userId, UserRole.USER);
        WithdrawUserRequest request = mock();
        User user = mock();
        BDDMockito.given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
        // when & then
        assertThrows(CustomException.class, () -> userCommandService.withdrawUserRequest(authUser, request),
                "비밀번호가 일치하지 않습니다.");
    }

    @Test
    void 사용자는_탈퇴를_할_수_있다() {
        // given
        Long userId = 1L;
        AuthUser authUser = AuthUser.toEntity(userId, UserRole.USER);
        User user = mock(User.class);
        WithdrawUserRequest request = new WithdrawUserRequest();
        ReflectionTestUtils.setField(request, "password", "test");

        BDDMockito.given(user.getPassword()).willReturn("test");
        BDDMockito.given(userRepository.findById(userId)).willReturn(Optional.of(user));
        BDDMockito.given(passwordEncoder.matches(request.getPassword(), "test")).willReturn(true);
        // when
        userCommandService.withdrawUserRequest(authUser, request);
        // then
        verify(userRepository, times(1)).delete(user);
    }

}