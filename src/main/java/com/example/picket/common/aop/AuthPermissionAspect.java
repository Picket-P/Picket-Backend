package com.example.picket.common.aop;

import com.example.picket.common.annotation.AuthPermission;
import com.example.picket.common.dto.AuthUser;
import com.example.picket.common.enums.UserRole;
import com.example.picket.common.exception.CustomException;
import com.example.picket.common.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
@RequiredArgsConstructor
public class AuthPermissionAspect {

    private final HttpServletRequest request;

    @Before("@annotation(com.example.picket.common.annotation.AuthPermission)")
    public void checkPermission(org.aspectj.lang.JoinPoint joinPoint) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            throw new CustomException(ErrorCode.USER_ROLE_FORBIDDEN_PERMISSION);
        }

        AuthUser authUser = (AuthUser) session.getAttribute("authUser");
        if (authUser == null) {
            throw new CustomException(ErrorCode.USER_ROLE_FORBIDDEN_PERMISSION);
        }

        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        AuthPermission authPermission = method.getAnnotation(AuthPermission.class);

        UserRole requiredRole = authPermission.role();
        if (!authUser.getUserRole().equals(requiredRole)) {
            throw new CustomException(ErrorCode.USER_ROLE_FORBIDDEN_PERMISSION); // 권한 없음
        }
    }
}