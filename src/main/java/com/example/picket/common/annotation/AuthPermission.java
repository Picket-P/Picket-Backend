package com.example.picket.common.annotation;

import com.example.picket.common.enums.UserRole;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AuthPermission {
    UserRole role();
}
