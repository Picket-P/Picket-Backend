package com.example.picket.domain.user.service;

import com.example.picket.common.dto.AuthUser;
import com.example.picket.common.exception.CustomException;
import com.example.picket.common.exception.ErrorCode;
import com.example.picket.config.PasswordEncoder;
import com.example.picket.domain.user.dto.request.UpdatePasswordRequest;
import com.example.picket.domain.user.dto.request.UpdateUserRequest;
import com.example.picket.domain.user.dto.request.WithdrawUserRequest;
import com.example.picket.domain.user.entity.User;
import com.example.picket.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserCommandService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User updateUser(AuthUser authUser, UpdateUserRequest request) {
        User user = userRepository.findById(authUser.getId()).orElseThrow(
                () -> new CustomException(ErrorCode.USER_NOT_FOUND)
        );

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.USER_PASSWORD_INVALID);
        }

        user.update(request.getNickname(), request.getProfileUrl());

        return user;
    }

    @Transactional
    public void updatePassword(AuthUser authUser, UpdatePasswordRequest request) {
        User user = userRepository.findById(authUser.getId()).orElseThrow(
                () -> new CustomException(ErrorCode.USER_NOT_FOUND)
        );

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.USER_PASSWORD_INVALID);
        }

        String encodePassword = passwordEncoder.encode(request.getNewPassword());

        user.passwordUpdate(encodePassword);
    }

    @Transactional
    public void withdrawUserRequest(AuthUser authUser, WithdrawUserRequest request) {
        User user = userRepository.findById(authUser.getId()).orElseThrow(
                () -> new CustomException(ErrorCode.USER_NOT_FOUND)
        );

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.USER_PASSWORD_INVALID);
        }

        userRepository.delete(user);
    }
}
