package com.example.picket.domain.auth.service;

import com.example.picket.common.dto.AuthUser;
import com.example.picket.common.enums.UserRole;
import com.example.picket.common.exception.CustomException;
import com.example.picket.common.exception.ErrorCode;
import com.example.picket.config.PasswordEncoder;
import com.example.picket.domain.auth.dto.request.SigninRequest;
import com.example.picket.domain.auth.dto.request.SignupRequest;
import com.example.picket.domain.auth.dto.response.SigninResponse;
import com.example.picket.domain.user.entity.User;
import com.example.picket.domain.user.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void signup(SignupRequest request, UserRole userRole) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        }

        User newUser = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .userRole(userRole)
                .nickname(request.getNickname())
                .birth(request.getBirth())
                .gender(request.getGender())
                .build();

        userRepository.save(newUser);
    }


    public SigninResponse signin(HttpSession session, SigninRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        AuthUser authUser = AuthUser.builder()
                .id(user.getId())
                .userRole(user.getUserRole())
                .build();

        session.setAttribute("authUser", authUser);

        return SigninResponse.builder()
                .sessionId(session.getId())
                .nickname(user.getNickname())
                .build();
    }

    public void signout(HttpSession session) {
        session.invalidate();
    }
}
