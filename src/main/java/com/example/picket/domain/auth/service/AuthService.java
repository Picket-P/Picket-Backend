package com.example.picket.domain.auth.service;

import com.example.picket.common.dto.AuthUser;
import com.example.picket.common.enums.Gender;
import com.example.picket.common.enums.UserRole;
import com.example.picket.common.exception.CustomException;
import com.example.picket.config.PasswordEncoder;
import com.example.picket.domain.auth.entity.TempUserData;
import com.example.picket.domain.email.service.EmailService;
import com.example.picket.domain.user.entity.User;
import com.example.picket.domain.user.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.springframework.http.HttpStatus.*;

@RequiredArgsConstructor
@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final StringRedisTemplate redisTemplate;

    // 회원가입 정보 등록 (이메일 인증 전)
    public String registerUserInfo(String email, String password, String nickname, LocalDate birth,
                                   Gender gender, UserRole userRole) {
        // 이미 가입된 이메일인지 확인
        if (userRepository.existsByEmail(email)) {
            throw new CustomException(BAD_REQUEST, "이미 가입되어있는 이메일입니다.");
        }

        // 임시 사용자 ID 생성
        String tempUserId = UUID.randomUUID().toString();

        // 임시 사용자 정보를 Redis에 저장 (30분 유효)
        TempUserData tempUserData = new TempUserData(email, passwordEncoder.encode(password),
                nickname, birth, gender, userRole);

        // Redis에 임시 사용자 정보 저장
        String tempUserKey = "tempUser:" + tempUserId;
        redisTemplate.opsForHash().put(tempUserKey, "email", email);
        redisTemplate.opsForHash().put(tempUserKey, "password", tempUserData.getPassword());
        redisTemplate.opsForHash().put(tempUserKey, "nickname", nickname);
        redisTemplate.opsForHash().put(tempUserKey, "birth", birth.toString());
        redisTemplate.opsForHash().put(tempUserKey, "gender", gender.toString());
        redisTemplate.opsForHash().put(tempUserKey, "userRole", userRole.toString());
        redisTemplate.expire(tempUserKey, 30, TimeUnit.MINUTES);

        // 이메일-임시ID 매핑 저장
        redisTemplate.opsForValue().set("email2tempId:" + email, tempUserId, 30, TimeUnit.MINUTES);

        // 이메일 인증 코드 발송
        emailService.sendVerificationEmail(email);

        return tempUserId;
    }

    // 이메일 인증 코드 발송
    public void sendVerificationCode(String email) {
        // 이미 가입된 이메일인지 확인
        if (userRepository.existsByEmail(email)) {
            throw new CustomException(BAD_REQUEST, "이미 가입되어있는 이메일입니다.");
        }

        // 회원가입 정보가 없는 경우 에러
        String tempUserId = redisTemplate.opsForValue().get("email2tempId:" + email);
        if (tempUserId == null) {
            throw new CustomException(BAD_REQUEST, "먼저 회원가입 정보를 입력해주세요.");
        }

        try {
            // 이메일 인증 코드 발송
            emailService.sendVerificationEmail(email);
        } catch (CustomException e) {
            throw e;
        }
    }

    // 이메일 인증 코드 검증 및 회원가입 완료
    public boolean verifyCodeAndCompleteSignup(String email, String code) {
        // 인증 코드 검증
        boolean isVerified = emailService.verifyCode(email, code);

        if (isVerified) {
            // 임시 사용자 ID 조회
            String tempUserId = redisTemplate.opsForValue().get("email2tempId:" + email);
            if (tempUserId == null) {
                throw new CustomException(BAD_REQUEST, "회원가입 정보가 만료되었습니다. 다시 시도해주세요.");
            }

            // 임시 사용자 정보 조회
            String tempUserKey = "tempUser:" + tempUserId;
            String storedEmail = (String) redisTemplate.opsForHash().get(tempUserKey, "email");
            String password = (String) redisTemplate.opsForHash().get(tempUserKey, "password");
            String nickname = (String) redisTemplate.opsForHash().get(tempUserKey, "nickname");
            String birthStr = (String) redisTemplate.opsForHash().get(tempUserKey, "birth");
            String genderStr = (String) redisTemplate.opsForHash().get(tempUserKey, "gender");
            String userRoleStr = (String) redisTemplate.opsForHash().get(tempUserKey, "userRole");

            if (storedEmail == null || !storedEmail.equals(email)) {
                throw new CustomException(BAD_REQUEST, "회원정보가 일치하지 않습니다.");
            }

            LocalDate birth = LocalDate.parse(birthStr);
            Gender gender = Gender.valueOf(genderStr);
            UserRole userRole = UserRole.valueOf(userRoleStr);

            // 사용자 생성 및 저장
            User newUser = User.create(email, password, userRole, null, nickname, birth, gender);
            userRepository.save(newUser);

            // Redis에서 임시 정보 삭제
            redisTemplate.delete(tempUserKey);
            redisTemplate.delete("email2tempId:" + email);
            redisTemplate.delete("verification:" + email);
            redisTemplate.delete("verified:" + email);

            return true;
        }

        return false;
    }

    public User signin(HttpSession session, HttpServletResponse response, String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(NOT_FOUND, "해당 유저를 찾을 수 없습니다."));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new CustomException(UNAUTHORIZED, "비밀번호가 일치하지 않습니다.");
        }

        AuthUser authUser = AuthUser.create(user.getId(), user.getUserRole());
        session.setAttribute("authUser", authUser);

        Cookie sessionCookie = new Cookie("JSESSIONID", session.getId());
        sessionCookie.setPath("/");
        sessionCookie.setMaxAge(24 * 60 * 60);
        sessionCookie.setHttpOnly(true);
        response.addCookie(sessionCookie);

        return user;
    }

    public void signout(HttpSession session, HttpServletResponse response) {
        Cookie sessionCookie = new Cookie("JSESSIONID", null);
        sessionCookie.setPath("/");
        sessionCookie.setMaxAge(0);
        response.addCookie(sessionCookie);
        session.invalidate();
    }
}