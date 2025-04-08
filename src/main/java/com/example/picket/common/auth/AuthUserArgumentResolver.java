package com.example.picket.common.auth;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

import com.example.picket.common.annotation.Auth;
import com.example.picket.common.dto.AuthUser;
import com.example.picket.common.exception.CustomException;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class AuthUserArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        boolean hasAuthAnnotation = parameter.getParameterAnnotation(Auth.class) != null;
        boolean isAuthUserType = parameter.getParameterType().equals(AuthUser.class);

        if (hasAuthAnnotation != isAuthUserType) {
            throw new CustomException(BAD_REQUEST, "@Auth와 AuthUser 타입은 함께 사용되어야 합니다.");
        }

        return hasAuthAnnotation;
    }

    @Override
    public Object resolveArgument(
            @Nullable MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            @Nullable NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory
    ) {
        HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();
        HttpSession session = request.getSession();

        return session == null ? false : (AuthUser) session.getAttribute("authUser");
    }

}
