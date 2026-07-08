package cn.wzpmc.filemanager.utils;

import cn.wzpmc.filemanager.annotation.Address;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * Address注解参数解析器
 */
@Slf4j
@Component
public class AddressArgumentResolver implements HandlerMethodArgumentResolver {
    /**
     * 通过多种方式（请求对端IP -> X-Real-IP -> X-Forwarded-For）获取请求方IP地址
     *
     * @param request 请求
     * @return IP地址
     */
    public static String getAddr(HttpServletRequest request) {
        String remoteAddr = request.getRemoteAddr();
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
        return remoteAddr;
    }

    /**
     * 适用于NativeWebRequest的请求地址解析器
     *
     * @param nativeRequest 请求
     * @return IP地址
     * @see AddressArgumentResolver#getAddr(HttpServletRequest)
     */
    public static String getAddr(NativeWebRequest nativeRequest) {
        if (nativeRequest instanceof ServletWebRequest servletWebRequest) {
            HttpServletRequest request = servletWebRequest.getRequest();
            return getAddr(request);
        }
        return "0.0.0.0";
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(Address.class);
    }

    @Override
    @Nullable
    public Object resolveArgument(@NonNull MethodParameter parameter, @Nullable ModelAndViewContainer mavContainer, @NonNull NativeWebRequest webRequest, @Nullable WebDataBinderFactory binderFactory) {
        return getAddr(webRequest);
    }
}
