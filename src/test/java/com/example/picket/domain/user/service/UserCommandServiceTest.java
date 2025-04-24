package com.example.picket.domain.user.service;

import com.example.picket.common.dto.AuthUser;
import com.example.picket.common.enums.UserRole;
import com.example.picket.common.exception.CustomException;
import com.example.picket.common.service.S3Service;
import com.example.picket.config.PasswordEncoder;
import com.example.picket.domain.images.dto.response.ImageResponse;
import com.example.picket.domain.images.entity.UserImage;
import com.example.picket.domain.images.repository.UserImageRepository;
import com.example.picket.domain.user.dto.request.UpdatePasswordRequest;
import com.example.picket.domain.user.dto.request.UpdateUserRequest;
import com.example.picket.domain.user.dto.request.WithdrawUserRequest;
import com.example.picket.domain.user.entity.User;
import com.example.picket.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserCommandServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    UserImageRepository userImageRepository;

    @Mock
    S3Service s3Service;

    @InjectMocks
    private UserCommandService userCommandService;

    @Nested
    class 유저_프로필_업데이트_테스트 {
        @Test
        void 존재하지않는_사용자를_조회시_예외처리를_던진다() {
            // given
            Long userId = 1L;
            AuthUser authUser = AuthUser.create(userId, UserRole.USER);
            UpdateUserRequest request = mock();

            given(userRepository.findById(anyLong())).willReturn(Optional.empty());
            // when & then
            assertThrows(CustomException.class, () -> userCommandService.updateUser(authUser, request),
                    "해당 유저를 찾을 수 없습니다."
            );
        }

        @Test
        void 유저정보_업데이트_시_입력한_비밀번호가_잘못되면_예외처리를_던진다() {
            // given
            Long userId = 1L;
            AuthUser authUser = AuthUser.create(userId, UserRole.USER);
            UpdateUserRequest request = mock();
            User user = mock();
            given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
            // when & then
            assertThrows(CustomException.class, () -> userCommandService.updateUser(authUser, request),
                    "비밀번호가 일치하지 않습니다.");
        }

        @Test
        void 사용자가_프로필을_변경할_수_있다() {
            // given
            Long userId = 1L;
            AuthUser authUser = AuthUser.create(userId, UserRole.USER);
            UpdateUserRequest request = new UpdateUserRequest();
            ReflectionTestUtils.setField(request, "password", "test");
            ReflectionTestUtils.setField(request, "nickname", "test");
            ReflectionTestUtils.setField(request, "profileUrl", "newProfileUrl");

            User user = mock(User.class);
            UserImage userImage = mock(UserImage.class);
            given(user.getPassword()).willReturn("test");
            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(passwordEncoder.matches("test", "test")).willReturn(true);
            given(userImageRepository.findByImageUrl(any())).willReturn(Optional.of(userImage));

            // when
            User response = userCommandService.updateUser(authUser, request);

            // then
            assertThat(response).isNotNull();
            verify(user).update("test", "newProfileUrl");
        }

        @Test
        void 사용자가_프로필을_변경시_기존_프로필_이미지가_존재한다면_제거하고_변경할_수_있다() {
            // given
            AuthUser authUser = mock(AuthUser.class);
            UpdateUserRequest request = mock(UpdateUserRequest.class);
            User user = mock(User.class);
            UserImage userImage = mock(UserImage.class);

            String existingProfileUrl = "https://example.com/old-image.jpg";
            String newProfileUrl = "https://example.com/new-image.jpg";
            String nickname = "newNickname";

            given(authUser.getId()).willReturn(1L);
            given(request.getPassword()).willReturn("password");
            given(request.getProfileUrl()).willReturn(newProfileUrl);
            given(request.getNickname()).willReturn(nickname);

            given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
            given(passwordEncoder.matches(any(), any())).willReturn(true);
            given(userImageRepository.findByImageUrl(any())).willReturn(Optional.of(userImage));
            given(user.getProfileUrl()).willReturn(existingProfileUrl);

            doNothing().when(userImage).updateUser(anyLong());
            doNothing().when(user).update(any(), any());
            doNothing().when(s3Service).delete(any());

            // when
            User response = userCommandService.updateUser(authUser, request);

            // then
            assertThat(response).isNotNull();
            verify(userRepository, times(1)).findById(anyLong());
            verify(passwordEncoder, times(1)).matches(any(), any());
            verify(userImageRepository, times(2)).findByImageUrl(any());
            verify(userImage, times(1)).updateUser(anyLong());
            verify(s3Service, times(1)).delete(existingProfileUrl);
            verify(user, times(1)).update(nickname, newProfileUrl);
        }
    }

    @Nested
    class 유저_비밀번호_업데이트_테스트 {
        @Test
        void 비밀번호_업데이트_시_존재하지_않는_사용자를_조회하면_예외처리를_던진다() {
            // given
            Long userId = 1L;
            AuthUser authUser = AuthUser.create(userId, UserRole.USER);
            UpdatePasswordRequest request = mock();

            given(userRepository.findById(anyLong())).willReturn(Optional.empty());
            // when & then
            assertThrows(CustomException.class, () -> userCommandService.updatePassword(authUser, request),
                    "해당 유저를 찾을 수 없습니다."
            );
        }

        @Test
        void 비밀번호_업데이트_시_입력한_비밀번호가_잘못되면_예외처리를_던진다() {
            // given
            Long userId = 1L;
            AuthUser authUser = AuthUser.create(userId, UserRole.USER);
            UpdatePasswordRequest request = mock();
            User user = mock();
            given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
            // when & then
            assertThrows(CustomException.class, () -> userCommandService.updatePassword(authUser, request),
                    "비밀번호가 일치하지 않습니다.");
        }

        @Test
        void 사용자의_비밀번호를_변경할_수_있다() {
            // given
            Long userId = 1L;
            AuthUser authUser = AuthUser.create(userId, UserRole.USER);
            UpdatePasswordRequest request = new UpdatePasswordRequest();
            ReflectionTestUtils.setField(request, "password", "test");
            ReflectionTestUtils.setField(request, "newPassword", "test2");

            User user = mock(User.class);
            given(user.getPassword()).willReturn("test");
            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(passwordEncoder.matches("test", "test")).willReturn(true);
            given(passwordEncoder.encode("test2")).willReturn("test2");

            // when
            userCommandService.updatePassword(authUser, request);

            // then
            verify(user).passwordUpdate("test2");
        }
    }

    @Nested
    class 유저_탈퇴_테스트 {
        @Test
        void 유저탈퇴_시_존재하지_않는_사용자를_조회하면_예외처리를_던진다() {
            // given
            Long userId = 1L;
            AuthUser authUser = AuthUser.create(userId, UserRole.USER);
            WithdrawUserRequest request = mock();

            given(userRepository.findById(anyLong())).willReturn(Optional.empty());
            // when & then
            assertThrows(CustomException.class, () -> userCommandService.withdrawUserRequest(authUser, request),
                    "해당 유저를 찾을 수 없습니다."
            );
        }

        @Test
        void 유저탈퇴_시_입력한_비밀번호가_잘못되면_예외처리를_던진다() {
            // given
            Long userId = 1L;
            AuthUser authUser = AuthUser.create(userId, UserRole.USER);
            WithdrawUserRequest request = mock();
            User user = mock();
            given(userRepository.findById(anyLong())).willReturn(Optional.of(user));
            // when & then
            assertThrows(CustomException.class, () -> userCommandService.withdrawUserRequest(authUser, request),
                    "비밀번호가 일치하지 않습니다.");
        }

        @Test
        void 사용자는_탈퇴를_할_수_있다() {
            // given
            Long userId = 1L;
            AuthUser authUser = AuthUser.create(userId, UserRole.USER);
            User user = mock(User.class);
            WithdrawUserRequest request = new WithdrawUserRequest();
            ReflectionTestUtils.setField(request, "password", "test");
            UserImage userImage = mock(UserImage.class);

            given(user.getPassword()).willReturn("test");
            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(passwordEncoder.matches(request.getPassword(), "test")).willReturn(true);
            given(userImageRepository.findByImageUrl(any())).willReturn(Optional.of(userImage));
            doNothing().when(userImageRepository).delete(any());
            // when
            userCommandService.withdrawUserRequest(authUser, request);
            // then
            verify(userRepository, times(1)).delete(user);
        }
    }

    @Nested
    class 유저_이미지_테스트 {
        @Test
        void 유저_포스터_이미지_생성_성공() {
            // given
            ImageResponse imageResponse = mock(ImageResponse.class);
            UserImage userImage = mock(UserImage.class);
            given(s3Service.upload(any(), anyLong(), any())).willReturn(imageResponse);
            given(userImageRepository.save(any())).willReturn(userImage);
            // when
            userCommandService.uploadImage(any(), anyLong(), any());
            // then
            verify(s3Service, times(1)).upload(any(), anyLong(), any());
            verify(userImageRepository, times(1)).save(any());
        }
    }
}