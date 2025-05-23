package com.example.picket.domain.user.service;

import com.example.picket.common.dto.AuthUser;
import com.example.picket.common.exception.CustomException;
import com.example.picket.domain.user.dto.response.UserResponse;
import com.example.picket.domain.user.entity.User;
import com.example.picket.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserQueryService {

    private final UserRepository userRepository;


    public UserResponse getUserResponse(AuthUser authUser) {
        User user = userRepository.findById(authUser.getId()).orElseThrow(
                () -> new CustomException(NOT_FOUND, "해당 유저를 찾을 수 없습니다.")
        );

        return UserResponse.of(user.getEmail(), user.getUserRole(), user.getProfileUrl(), user.getNickname(),
                user.getBirth(), user.getGender());
    }

    public User getUser(Long id) {
        return userRepository.findById(id).orElseThrow(
                () -> new CustomException(NOT_FOUND, "해당 유저를 찾을 수 없습니다."));
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmailAndDeletedAtNull(email);
    }
}
