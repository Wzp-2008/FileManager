package cn.wzpmc.filemanager.configuration;

import cn.wzpmc.filemanager.entities.vo.FileVo;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
@Getter
public class RedisConfiguration {
    private final RedisConnectionFactory redisConnectionFactory;

    @Autowired
    public RedisConfiguration(RedisConnectionFactory redisConnectionFactory) {
        this.redisConnectionFactory = redisConnectionFactory;
    }
    @Bean
    public RedisTemplate<String, FileVo> linkMapper() {
        RedisTemplate<String, FileVo> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        return template;
    }
}