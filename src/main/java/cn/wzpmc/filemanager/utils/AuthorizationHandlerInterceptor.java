package cn.wzpmc.filemanager.utils;

import cn.wzpmc.filemanager.annotation.AuthorizationRequired;
import cn.wzpmc.filemanager.exceptions.ResponseException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Authorization注解的请求拦截器
 */
@Component
public class AuthorizationHandlerInterceptor implements HandlerInterceptor {
    private final AuthorizationUtils authorizationUtils;

    @Autowired
    public AuthorizationHandlerInterceptor(AuthorizationUtils authorizationUtils) {
        this.authorizationUtils = authorizationUtils;
    }

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws ResponseException {
        if (handler instanceof HandlerMethod method) {
            if (!method.hasMethodAnnotation(AuthorizationRequired.class)) {
                return true;
            }
            AuthorizationRequired annotation = method.getMethodAnnotation(AuthorizationRequired.class);
            return authorizationUtils.auth(request, response, annotation);
        }
        return true;
    }
}