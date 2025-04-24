package com.example.picket.domain.user.service;

import com.example.picket.common.dto.AuthUser;
import com.example.picket.common.enums.Gender;
import com.example.picket.common.enums.UserRole;
import com.example.picket.common.exception.CustomException;
import com.example.picket.domain.user.dto.response.UserResponse;
import com.example.picket.domain.user.entity.User;
import com.example.picket.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class UserQueryServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserQueryService userQueryService;

    @Test
    void 존재하지않는_사용자를_조회시_예외처리를_던진다() {
        // given
        Long userId = 1L;
        AuthUser authUser = AuthUser.create(userId, UserRole.USER);

        given(userRepository.findById(anyLong())).willReturn(Optional.empty());
        // when & then
        assertThrows(CustomException.class, () -> userQueryService.getUserResponse(authUser), "해당 유저를 찾을 수 없습니다.");
    }

    @Test
    void 사용자를_조회한다() {
        // given
        Long userId = 1L;
        AuthUser authUser = AuthUser.create(userId, UserRole.USER);
        User user = mock();
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        // when
        UserResponse userResponse = userQueryService.getUserResponse(authUser);
        // then
        assertThat(userResponse).isNotNull();
    }


    @Test
    void getUser_존재하는_유저_조회_성공() {
        // given
        Long userId = 1L;
        User user = createUser(userId);
        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));

        // when
        User result = userQueryService.getUser(userId);

        // then
        assertThat(result).isEqualTo(user);
    }

    @Test
    void getUser_존재하지않는_유저_조회시_예외발생() {
        // given
        Long userId = 999L;
        given(userRepository.findById(anyLong())).willReturn(Optional.empty());

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> {
            userQueryService.getUser(userId);
        });

        assertEquals("해당 유저를 찾을 수 없습니다.", exception.getMessage());
    }

    @Test
    void getUserByEmail_삭제되지않은_유저_조회_성공() {
        // given
        Long userId = 1L;
        User user = createUser(userId);
        given(userRepository.findByEmailAndDeletedAtNull(anyString())).willReturn(Optional.of(user));

        // when
        Optional<User> result = userQueryService.getUserByEmail(user.getEmail());

        // then
        assertEquals(user, result.get());
        assertEquals(user.getEmail(), result.get().getEmail());
        assertEquals(user.getUserRole(), result.get().getUserRole());
    }

    @Test
    void getUserByEmail_없는_경우_Optional_빈값_반환() {
        // given
        String email = "unknown@example.com";
        given(userRepository.findByEmailAndDeletedAtNull(anyString())).willReturn(Optional.empty());

        // when
        Optional<User> result = userQueryService.getUserByEmail(email);

        // then
        assertThat(result).isNotPresent();
    }

    private User createUser(Long userId) {
        User user = User.create("user@example.com"
                , "test123!"
                , UserRole.USER
                , null
                , "닉네임"
                , LocalDate.parse("1990-06-25")
                , Gender.FEMALE);

        ReflectionTestUtils.setField(user, "id", userId);

        return user;
    }

}