package com.example.picket.domain.auth.service;

import com.example.picket.common.dto.AuthUser;
import com.example.picket.common.enums.Gender;
import com.example.picket.common.enums.UserRole;
import com.example.picket.common.exception.CustomException;
import com.example.picket.common.exception.ErrorCode;
import com.example.picket.config.PasswordEncoder;
import com.example.picket.domain.user.entity.User;
import com.example.picket.domain.user.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void signup(String email, String password, String nickname, LocalDate birth, Gender gender,
                       UserRole userRole) {

        if (userRepository.existsByEmail(email)) {
            throw new CustomException(ErrorCode.USER_DUPLICATE_EMAIL);
        }

        User newUser = User.toEntity(email, passwordEncoder.encode(password), userRole, null, nickname, birth, gender);

        userRepository.save(newUser);
    }

    public User signin(HttpSession session, String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new CustomException(ErrorCode.USER_PASSWORD_INVALID);
        }

        AuthUser authUser = AuthUser.toEntity(user.getId(), user.getUserRole());
        session.setAttribute("authUser", authUser);

        return user;
    }

    public void signout(HttpSession session) {
        session.invalidate();
    }
}
