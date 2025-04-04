package com.example.picket.common.interceptor;

import com.example.picket.common.exception.CustomException;
import com.example.picket.common.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        HttpSession session = request.getSession(false);

        if (session == null) {
            throw new CustomException(ErrorCode.AUTH_UNAUTHORIZED_LOGIN);
        }

        return true;
    }
}
