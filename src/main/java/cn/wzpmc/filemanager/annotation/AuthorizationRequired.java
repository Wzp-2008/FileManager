package cn.wzpmc.filemanager.annotation;

import cn.wzpmc.filemanager.entities.user.enums.Auth;

import java.lang.annotation.*;

@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface AuthorizationRequired {
    Auth level() default Auth.user;
    boolean force() default false;
}