package cn.wzpmc.filemanager.configuration;

import cn.wzpmc.filemanager.utils.AuthorizationArgumentResolver;
import cn.wzpmc.filemanager.utils.AuthorizationHandlerInterceptor;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * 用于@AuthorizationRequired注解的用户token登录参数解析器/拦截器
 *
 * @see cn.wzpmc.filemanager.annotation.AuthorizationRequired
 */
@Configuration
public class AuthorizationConfiguration implements WebMvcConfigurer {
    private final AuthorizationArgumentResolver authorizationArgumentResolver;
    private final AuthorizationHandlerInterceptor authorizationHandlerInterceptor;

    @Autowired
    public AuthorizationConfiguration(AuthorizationArgumentResolver authorizationArgumentResolver, AuthorizationHandlerInterceptor authorizationHandlerInterceptor) {
        this.authorizationArgumentResolver = authorizationArgumentResolver;
        this.authorizationHandlerInterceptor = authorizationHandlerInterceptor;
    }

    /**
     * 添加token参数解析器
     */
    @Override
    public void addArgumentResolvers(@NonNull List<HandlerMethodArgumentResolver> resolvers) {
        WebMvcConfigurer.super.addArgumentResolvers(resolvers);
        resolvers.add(authorizationArgumentResolver);
    }

    /**
     * 添加登录拦截器
     */
    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        WebMvcConfigurer.super.addInterceptors(registry);
        registry.addInterceptor(authorizationHandlerInterceptor);
    }
}