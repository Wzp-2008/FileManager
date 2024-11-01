package cn.wzpmc.filemanager.configuration;

import cn.wzpmc.filemanager.entities.files.ChunkChecked;
import cn.wzpmc.filemanager.entities.files.ChunkReady;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
public class RedisConfiguration {
    private final RedisConnectionFactory redisConnectionFactory;

    @Autowired
    public RedisConfiguration(RedisConnectionFactory redisConnectionFactory) {
        this.redisConnectionFactory = redisConnectionFactory;
    }
    @Bean
    public RedisTemplate<String, ChunkReady> uploadMapper() {
        RedisTemplate<String, ChunkReady> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        return template;
    }
    @Bean
    public RedisTemplate<String, ChunkChecked> chunkUploadMapper() {
        RedisTemplate<String, ChunkChecked> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        return template;
    }
}