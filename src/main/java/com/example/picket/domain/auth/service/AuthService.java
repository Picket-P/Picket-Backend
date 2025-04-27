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
import java.util.Map;
import java.util.Optional;
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

        if (userRepository.existsByEmail(email)) {
            throw new CustomException(BAD_REQUEST, "이미 가입되어있는 이메일입니다.");
        }

        String tempUserId = UUID.randomUUID().toString();
        String tempUserKey = "tempUser:" + tempUserId;

        TempUserData tempUserData = new TempUserData(email, passwordEncoder.encode(password),
                nickname, birth, gender, userRole);

        // Redis에 임시 사용자 정보 저장 (30분 유효)
        redisTemplate.opsForHash().put(tempUserKey, "email", email);
        redisTemplate.opsForHash().put(tempUserKey, "password", tempUserData.getPassword());
        redisTemplate.opsForHash().put(tempUserKey, "nickname", nickname);
        redisTemplate.opsForHash().put(tempUserKey, "birth", birth.toString());
        redisTemplate.opsForHash().put(tempUserKey, "gender", gender.toString());
        redisTemplate.opsForHash().put(tempUserKey, "userRole", userRole.toString());
        redisTemplate.expire(tempUserKey, 30, TimeUnit.MINUTES);

        redisTemplate.opsForValue().set("email2tempId:" + email, tempUserId, 30, TimeUnit.MINUTES);

        // 이메일 인증 코드 발송
        emailService.sendVerificationEmail(email);

        return tempUserId;
    }

    // 이메일 인증 코드 검증 및 회원가입 완료
    public boolean verifyCodeAndCompleteSignup(String email, String code) {

        boolean isVerified = emailService.verifyCode(email, code);

        if (!isVerified) {
            return false;
        }

        String tempUserId = Optional.ofNullable(redisTemplate.opsForValue().get("email2tempId:" + email))
                .orElseThrow(() -> new CustomException(BAD_REQUEST, "회원가입 정보가 만료되었습니다. 다시 시도해주세요."));

        String tempUserKey = "tempUser:" + tempUserId;

        Map<Object, Object> userDataMap = redisTemplate.opsForHash().entries(tempUserKey);
        String storedEmail = (String) userDataMap.get("email");

        if (storedEmail == null || !storedEmail.equals(email)) {
            throw new CustomException(BAD_REQUEST, "회원정보가 일치하지 않습니다.");
        }

        User newUser = createUserFromRedisData(userDataMap);
        userRepository.save(newUser);

        cleanupRedisData(tempUserKey, email);

        return true;
    }

    // Redis 데이터로부터 User 객체 생성
    private User createUserFromRedisData(Map<Object, Object> userDataMap) {
        String email = (String) userDataMap.get("email");
        String password = (String) userDataMap.get("password");
        String nickname = (String) userDataMap.get("nickname");
        LocalDate birth = LocalDate.parse((String) userDataMap.get("birth"));
        Gender gender = Gender.valueOf((String) userDataMap.get("gender"));
        UserRole userRole = UserRole.valueOf((String) userDataMap.get("userRole"));

        return User.create(email, password, userRole, null, nickname, birth, gender);
    }

    // Redis 임시 데이터 정리
    private void cleanupRedisData(String tempUserKey, String email) {
        redisTemplate.delete(tempUserKey);
        redisTemplate.delete("email2tempId:" + email);
        redisTemplate.delete("verification:" + email);
        redisTemplate.delete("verified:" + email);
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