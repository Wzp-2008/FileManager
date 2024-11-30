package cn.wzpmc.filemanager.utils;

import cn.wzpmc.filemanager.annotation.Address;
import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Slf4j
@Component
public class AddressArgumentResolver implements HandlerMethodArgumentResolver {
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(Address.class);
    }

    @Override
    @Nullable
    public Object resolveArgument(@NonNull MethodParameter parameter, @Nullable ModelAndViewContainer mavContainer, @NonNull NativeWebRequest webRequest, @Nullable WebDataBinderFactory binderFactory) {
        if (webRequest instanceof ServletWebRequest servletWebRequest) {
            return servletWebRequest.getRequest().getRemoteAddr();
        }
        return null;
    }
}
