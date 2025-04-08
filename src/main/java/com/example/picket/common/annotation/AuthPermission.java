package com.example.picket.common.annotation;

import com.example.picket.common.enums.UserRole;
import io.swagger.v3.oas.annotations.Parameter;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Parameter(hidden = true)
public @interface AuthPermission {
    UserRole role();
}
