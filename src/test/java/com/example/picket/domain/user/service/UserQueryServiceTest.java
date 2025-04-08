package com.example.picket.domain.user.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;

import com.example.picket.common.dto.AuthUser;
import com.example.picket.common.enums.UserRole;
import com.example.picket.common.exception.CustomException;
import com.example.picket.domain.user.dto.response.UserResponse;
import com.example.picket.domain.user.entity.User;
import com.example.picket.domain.user.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
        AuthUser authUser = AuthUser.toEntity(userId, UserRole.USER);

        BDDMockito.given(userRepository.findById(anyLong())).willReturn(Optional.empty());
        // when & then
        assertThrows(CustomException.class, () -> userQueryService.getUserResponse(authUser), "해당 유저를 찾을 수 없습니다.");
    }

    @Test
    void 사용자를_조회한다() {
        // given
        Long userId = 1L;
        AuthUser authUser = AuthUser.toEntity(userId, UserRole.USER);
        User user = mock();
        BDDMockito.given(userRepository.findById(userId)).willReturn(Optional.of(user));
        // when
        UserResponse userResponse = userQueryService.getUserResponse(authUser);
        // then
        assertThat(userResponse).isNotNull();
    }

}