package com.example.picket.common.interceptor;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import com.example.picket.common.consts.Const;
import com.example.picket.common.exception.CustomException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.util.PatternMatchUtils;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        HttpSession session = request.getSession(false);

        String requestMethod = request.getMethod();
        String requestUri = request.getRequestURI();

        if (isWhiteList(requestMethod, requestUri)) {
            return true;
        }

        if (session == null || session.getAttribute("authUser") == null) {
            throw new CustomException(UNAUTHORIZED, "로그인 이후 이용 가능합니다.");
        }

        return true;
    }

    private boolean isWhiteList(String method, String path) {
        if (!Const.WHITE_LIST.containsKey(method)) {
            return false;
        }

        String[] lists = Const.WHITE_LIST.get(method);
        return PatternMatchUtils.simpleMatch(lists, path);
    }
}
