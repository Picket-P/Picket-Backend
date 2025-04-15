package com.example.picket.common.aop;

import com.example.picket.common.annotation.AuthPermission;
import com.example.picket.common.dto.AuthUser;
import com.example.picket.common.enums.UserRole;
import com.example.picket.common.exception.CustomException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class AuthPermissionAspect {

    private final HttpServletRequest request;

    @Before("@annotation(authPermission)")
    public void checkPermission(AuthPermission authPermission) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            throw new CustomException(HttpStatus.FORBIDDEN, "이 작업을 수행할 권한이 없습니다.");
        }

        AuthUser authUser = (AuthUser) session.getAttribute("authUser");
        if (authUser == null) {
            throw new CustomException(HttpStatus.FORBIDDEN, "이 작업을 수행할 권한이 없습니다.");
        }

        UserRole requiredRole = authPermission.role();
        if (!authUser.getUserRole().equals(requiredRole)) {
            throw new CustomException(HttpStatus.FORBIDDEN, "이 작업을 수행할 권한이 없습니다.");
        }
    }
}