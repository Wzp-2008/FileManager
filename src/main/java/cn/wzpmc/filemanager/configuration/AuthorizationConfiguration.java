package cn.wzpmc.filemanager.configuration;

import cn.wzpmc.filemanager.utils.AuthorizationArgumentResolver;
import cn.wzpmc.filemanager.utils.AuthorizationHandlerInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class AuthorizationConfiguration implements WebMvcConfigurer {
    private final AuthorizationArgumentResolver authorizationArgumentResolver;
    private final AuthorizationHandlerInterceptor authorizationHandlerInterceptor;
    @Autowired
    public AuthorizationConfiguration(AuthorizationArgumentResolver authorizationArgumentResolver, AuthorizationHandlerInterceptor authorizationHandlerInterceptor) {
        this.authorizationArgumentResolver = authorizationArgumentResolver;
        this.authorizationHandlerInterceptor = authorizationHandlerInterceptor;
    }
    @Override
    public void addArgumentResolvers(@NonNull List<HandlerMethodArgumentResolver> resolvers) {
        WebMvcConfigurer.super.addArgumentResolvers(resolvers);
        resolvers.add(authorizationArgumentResolver);
    }

    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        WebMvcConfigurer.super.addInterceptors(registry);
        registry.addInterceptor(authorizationHandlerInterceptor);
    }
}