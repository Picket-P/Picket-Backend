package com.example.picket.domain.user.service;

import com.example.picket.common.exception.CustomException;
import com.example.picket.common.exception.ErrorCode;
import com.example.picket.config.PasswordEncoder;
import com.example.picket.domain.user.dto.request.UpdatePasswordRequest;
import com.example.picket.domain.user.dto.request.UpdateUserRequest;
import com.example.picket.domain.user.dto.request.WithdrawUserRequest;
import com.example.picket.domain.user.dto.response.UserResponse;
import com.example.picket.domain.user.entity.User;
import com.example.picket.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public UserResponse getUser() {
        User user = userRepository.findById().orElseThrow(
                () -> new CustomException(ErrorCode.CANNOT_FIND_USER)
        );

        return new UserResponse(user.getEmail(), user.getUserRole(), user.getProfileUrl(), user.getNickname(), user.getBirth(), user.getGender());
    }
}
