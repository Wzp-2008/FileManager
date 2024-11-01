package cn.wzpmc.filemanager.utils;

import cn.wzpmc.filemanager.annotation.AuthorizationRequired;
import cn.wzpmc.filemanager.exceptions.AuthorizationException;
import jakarta.annotation.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class AuthorizationArgumentResolver implements HandlerMethodArgumentResolver {
    private final AuthorizationUtils authorizationUtils;
    @Autowired
    public AuthorizationArgumentResolver(AuthorizationUtils authorizationUtils){
        this.authorizationUtils = authorizationUtils;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(AuthorizationRequired.class);
    }

    @Override
    @Nullable
    public Object resolveArgument(@NonNull MethodParameter parameter, @Nullable ModelAndViewContainer mavContainer, @NonNull NativeWebRequest webRequest, @Nullable WebDataBinderFactory binderFactory) throws AuthorizationException {
        AuthorizationRequired parameterAnnotation = parameter.getParameterAnnotation(AuthorizationRequired.class);
        assert parameterAnnotation != null;
        return this.authorizationUtils.auth(webRequest, parameterAnnotation);
    }
}