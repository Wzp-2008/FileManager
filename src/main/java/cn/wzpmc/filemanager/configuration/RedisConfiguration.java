package cn.wzpmc.filemanager.configuration;

import cn.wzpmc.filemanager.entities.p2p.RawChannelDescription;
import cn.wzpmc.filemanager.entities.vo.FileVo;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * redis数据库相关配置
 */
@Configuration
@Getter
public class RedisConfiguration {
    private final RedisConnectionFactory redisConnectionFactory;

    @Autowired
    public RedisConfiguration(RedisConnectionFactory redisConnectionFactory) {
        this.redisConnectionFactory = redisConnectionFactory;
    }

    /**
     * @return 下载链接存储仓库
     */
    @Bean
    public RedisTemplate<String, FileVo> linkMapper() {
        RedisTemplate<String, FileVo> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        return template;
    }

    /**
     * @return P2P文件传输通道仓库
     */
    @Bean
    public RedisTemplate<String, RawChannelDescription> channelMapper() {
        RedisTemplate<String, RawChannelDescription> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        return template;
    }
}