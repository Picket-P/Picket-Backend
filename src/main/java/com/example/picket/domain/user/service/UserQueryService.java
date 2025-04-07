package com.example.picket.domain.user.service;

import com.example.picket.common.dto.AuthUser;
import com.example.picket.common.exception.CustomException;
import com.example.picket.common.exception.ErrorCode;
import com.example.picket.domain.user.dto.response.UserResponse;
import com.example.picket.domain.user.entity.User;
import com.example.picket.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserQueryService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserResponse getUser(AuthUser authUser) {
        User user = userRepository.findById(authUser.getId()).orElseThrow(
                () -> new CustomException(ErrorCode.USER_NOT_FOUND)
        );

        return UserResponse.toDto(user.getEmail(), user.getUserRole(), user.getProfileUrl(), user.getNickname(), user.getBirth(), user.getGender());
    }
}
