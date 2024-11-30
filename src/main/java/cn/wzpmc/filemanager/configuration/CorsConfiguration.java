package cn.wzpmc.filemanager.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfiguration implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") //允许所有路径进行CORS
                // .maxAge(300 * 1000)
                // .allowedOrigins("https://localhost:5173", "https://127.0.0.1:5173", "http://localhost:5173", "http://127.0.0.1:5173", "http://192.168.31.12:5173", "https://192.168.31.12:5173", "https://local.wzpmc.cn:5173")
                .allowedHeaders("*")
                .exposedHeaders("Add-Authorization")
                .allowedMethods("*"); //允许所有请求方式

    }
}