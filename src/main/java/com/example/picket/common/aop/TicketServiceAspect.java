package com.example.picket.common.aop;

import static org.springframework.http.HttpStatus.FORBIDDEN;

import com.example.picket.common.enums.UserRole;
import com.example.picket.common.exception.CustomException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class TicketServiceAspect {

    @Before("execution(* com.example.picket.domain.ticket.service.TicketCommandService.createTicket(..))")
    public void checkUserRoleBeforeCreateTicket(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        Parameter[] parameters = method.getParameters();
        Object[] args = joinPoint.getArgs();

        UserRole userRole = null;
        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].getType().equals(UserRole.class)) {
                userRole = (UserRole) args[i];
                break;
            }
        }

        if (userRole == null) {
            throw new IllegalStateException("UserRole 파라미터가 createTicket 메서드에 존재하지 않습니다.");
        }

        if (userRole != UserRole.USER) {
            throw new CustomException(FORBIDDEN, "오직 USER만 예매 가능합니다.");
        }
    }
}
