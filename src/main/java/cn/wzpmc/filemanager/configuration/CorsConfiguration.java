package cn.wzpmc.filemanager.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 跨域配置
 */
@Configuration
public class CorsConfiguration implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") //允许所有路径进行CORS
                .allowedHeaders("*") // 允许所有头传输
                .exposedHeaders("Add-Authorization") // 暴露Add-Authorization头
                .allowedMethods("*"); //允许所有请求方式

    }
}