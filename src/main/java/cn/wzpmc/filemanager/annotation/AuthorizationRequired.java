package cn.wzpmc.filemanager.annotation;

import cn.wzpmc.filemanager.entities.user.enums.Auth;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface AuthorizationRequired {
    Auth level() default Auth.user;
    boolean force() default false;
}