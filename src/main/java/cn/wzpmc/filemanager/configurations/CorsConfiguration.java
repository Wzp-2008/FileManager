package cn.wzpmc.filemanager.configurations;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfiguration implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") //**匹配的是我们所有后台的路径，代表后台共享了什么资源
                .maxAge(300 * 1000)
                .allowedOrigins("https://localhost:5173", "https://127.0.0.1:5173", "http://localhost:5173", "http://127.0.0.1:5173", "http://192.168.31.12:5173", "https://192.168.31.12:5173")
                .allowedHeaders("*")
                .exposedHeaders("Set-Authorization")
                .allowedMethods("*"); //允许的前台的请求方式

    }
}
