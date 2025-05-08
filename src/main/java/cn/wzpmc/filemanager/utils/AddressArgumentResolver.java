package cn.wzpmc.filemanager.utils;

import cn.wzpmc.filemanager.annotation.Address;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
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
        String remoteAddr = "0.0.0.0";
        if (webRequest instanceof ServletWebRequest servletWebRequest) {
            HttpServletRequest request = servletWebRequest.getRequest();
            remoteAddr = request.getRemoteAddr();
            if (remoteAddr.equals("127.0.0.1")) {
                String header = request.getHeader("X-Real-IP");
                if (header != null) {
                    remoteAddr = header;
                }
            }
            if (remoteAddr.equals("127.0.0.1")) {
                String xff = request.getHeader("X-Forwarded-For");
                if (xff != null) {
                    remoteAddr = xff;
                }
            }
        }
        return remoteAddr;
    }
}
