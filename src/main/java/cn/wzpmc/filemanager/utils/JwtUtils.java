package cn.wzpmc.filemanager.utils;

import cn.wzpmc.filemanager.config.FileManagerProperties;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Optional;

/**
 * Jwt工具类
 */
@Component
@Log4j2
public class JwtUtils {
    /**
     * jwt使用的hmac密钥以及算法
     */
    @Getter
    private final Algorithm hmacKey;
    private final RandomUtils randomUtils;

    @Autowired
    public JwtUtils(FileManagerProperties properties, RandomUtils randomUtils) {
        this.randomUtils = randomUtils;
        // 从用户配置中获取hmac密钥
        String hmacKey = properties.getHmacKey();
        String key;
        // 如果配置的hmac密钥为RANDOM
        if ("RANDOM".equalsIgnoreCase(hmacKey)) {
            // 生成随机的hmac密钥
            key = this.generatorHmacKey();
            log.info("Using Random Hmac Key: {}", key);
        } else {
            // 否则使用配置的密钥
            key = hmacKey;
        }
        // 使用hmac512算法并绑定该密钥
        this.hmacKey = Algorithm.HMAC512(key);
    }

    /**
     * 随机生成hmac密钥
     *
     * @return 16位随机字符
     * @see RandomUtils#generatorRandomString(int)
     */
    private String generatorHmacKey() {
        return this.randomUtils.generatorRandomString(16);
    }

    /**
     * 根据用户ID生成token
     *
     * @param uid 用户id
     * @return 有效期5天的jwt token
     */
    public String createToken(long uid) {
        Calendar instance = Calendar.getInstance();
        instance.add(Calendar.HOUR, 24 * 5);
        JWTCreator.Builder builder = JWT.create();
        builder.withClaim("uid", uid);
        builder.withExpiresAt(instance.getTime());
        return builder.sign(this.hmacKey);
    }

    /**
     * 根据token获取用户id
     *
     * @param token 对应token
     * @return 用户ID
     */
    public Optional<Integer> getUser(String token) {
        DecodedJWT verify;
        try {
            verify = JWT.require(this.hmacKey).build().verify(token);
        } catch (Exception e) {
            return Optional.empty();
        }
        Claim uid = verify.getClaim("uid");
        return Optional.of(uid.asInt());
    }
}