package cn.wzpmc.filemanager.configuration;

import cn.wzpmc.filemanager.entities.statistics.enums.Actions;
import cn.wzpmc.filemanager.entities.vo.UserVo;
import cn.wzpmc.filemanager.service.StatisticsService;
import cn.wzpmc.filemanager.utils.AddressArgumentResolver;
import cn.wzpmc.filemanager.utils.JwtUtils;
import com.alibaba.fastjson2.JSONObject;
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

/**
 * 静态资源配置
 */
@Configuration
@RequiredArgsConstructor
public class StaticResourceConfiguration implements WebMvcConfigurer, HandlerInterceptor {
    private final StatisticsService accessService;
    private final JwtUtils jwtUtils;

    /**
     * 设置静态资源位置为运行目录下的static文件夹
     */
    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/*")
                .addResourceLocations("./static/");
    }

    /**
     * 设置访问/index.html的拦截器，用于记录访问日志
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(this)
                .addPathPatterns("/index.html");
    }

    /**
     * 拦截index.html的访问用于记录日志
     */
    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        String remoteAddr = AddressArgumentResolver.getAddr(request);
        String authorization = request.getHeader("Authorization");
        // 若用户已登录，则记录用户ID，否则不记录用户ID，只记录IP
        if (authorization != null) {
            Optional<Integer> user = jwtUtils.getUser(authorization);
            if (user.isPresent()) {
                this.accessService.insertAction(new UserVo(user.get()), Actions.ACCESS, JSONObject.of("remoteAddr", remoteAddr));
                return HandlerInterceptor.super.preHandle(request, response, handler);
            }
        }
        this.accessService.insertAction(Actions.ACCESS, JSONObject.of("remoteAddr", remoteAddr));
        return HandlerInterceptor.super.preHandle(request, response, handler);
    }
}