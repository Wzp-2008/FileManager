package cn.wzpmc.filemanager.configuration;

import cn.wzpmc.filemanager.utils.AddressArgumentResolver;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * IP地址参数解析器
 */
@Configuration
@RequiredArgsConstructor
public class AddressConfiguration implements WebMvcConfigurer {
    private final AddressArgumentResolver addressArgumentResolver;

    /**
     * 添加用于解析客户端IP地址的参数解析器
     *
     * @param resolvers initially an empty list
     */
    @Override
    public void addArgumentResolvers(@NonNull List<HandlerMethodArgumentResolver> resolvers) {
        WebMvcConfigurer.super.addArgumentResolvers(resolvers);
        resolvers.add(addressArgumentResolver);
    }
}
