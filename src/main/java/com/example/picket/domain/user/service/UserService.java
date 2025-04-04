package com.example.picket.domain.user.service;

import com.example.picket.common.dto.AuthUser;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public UserResponse getUser(AuthUser authUser) {
        User user = userRepository.findById(authUser.getId()).orElseThrow(
                () -> new CustomException(ErrorCode.NOT_FOUND_USER)
        );

        return new UserResponse(user.getEmail(), user.getUserRole(), user.getProfileUrl(), user.getNickname(), user.getBirth(), user.getGender());
    }

    @Transactional
    public UserResponse updateUser(AuthUser authUser, UpdateUserRequest request) {
        User user = userRepository.findById(authUser.getId()).orElseThrow(
                () -> new CustomException(ErrorCode.NOT_FOUND_USER)
        );

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        user.update(request.getNickname(), request.getProfileUrl());

        return new UserResponse(user.getEmail(), user.getUserRole(), user.getProfileUrl(), user.getNickname(), user.getBirth(), user.getGender());
    }

    @Transactional
    public void updatePassword(AuthUser authUser, UpdatePasswordRequest request) {
        User user = userRepository.findById(authUser.getId()).orElseThrow(
                () -> new CustomException(ErrorCode.NOT_FOUND_USER)
        );

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        String encodePassword = passwordEncoder.encode(request.getNewPassword());

        user.passwordUpdate(encodePassword);
    }

    @Transactional
    public void withdrawUserRequest(AuthUser authUser, WithdrawUserRequest request) {
        User user = userRepository.findById(authUser.getId()).orElseThrow(
                () -> new CustomException(ErrorCode.NOT_FOUND_USER)
        );

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        userRepository.delete(user);
    }
}
