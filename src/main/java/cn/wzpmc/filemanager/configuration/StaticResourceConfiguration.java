package cn.wzpmc.filemanager.configuration;

import cn.wzpmc.filemanager.entities.statistics.enums.Actions;
import cn.wzpmc.filemanager.entities.vo.UserVo;
import cn.wzpmc.filemanager.service.StatisticsService;
import cn.wzpmc.filemanager.utils.AddressArgumentResolver;
import cn.wzpmc.filemanager.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Optional;

@Configuration
@RequiredArgsConstructor
public class StaticResourceConfiguration implements WebMvcConfigurer, HandlerInterceptor {
    private final StatisticsService accessService;
    private final JwtUtils jwtUtils;

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/*")
                .addResourceLocations("./static/");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(this)
                .addPathPatterns("/index.html");
    }

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        String remoteAddr = AddressArgumentResolver.getAddr(request);
        String authorization = request.getHeader("Authorization");
        if (authorization != null) {
            Optional<Integer> user = jwtUtils.getUser(authorization);
            if (user.isPresent()) {
                this.accessService.insertAction(new UserVo(user.get()), Actions.ACCESS, remoteAddr);
                return HandlerInterceptor.super.preHandle(request, response, handler);
            }
        }
        this.accessService.insertAction(Actions.ACCESS, remoteAddr);
        return HandlerInterceptor.super.preHandle(request, response, handler);
    }
}