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

@Component
@Log4j2
public class JwtUtils {
    @Getter
    private final Algorithm hmacKey;
    private final RandomUtils randomUtils;
    private String generatorHmacKey(){
        return this.randomUtils.generatorRandomString(16);
    }
    @Autowired
    public JwtUtils(FileManagerProperties properties, RandomUtils randomUtils){
        this.randomUtils = randomUtils;
        String hmacKey = properties.getHmacKey();
        String key;
        if ("RANDOM".equalsIgnoreCase(hmacKey)){
            key = this.generatorHmacKey();
            log.info("Using Random Hmac Key: {}", key);
        }else{
            key = hmacKey;
        }
        this.hmacKey = Algorithm.HMAC512(key);
    }
    public String createToken(long uid){
        Calendar instance = Calendar.getInstance();
        instance.add(Calendar.HOUR,24 * 5);
        JWTCreator.Builder builder = JWT.create();
        builder.withClaim("uid", uid);
        builder.withExpiresAt(instance.getTime());
        return builder.sign(this.hmacKey);
    }
    public Optional<Integer> getUser(String token){
        DecodedJWT verify;
        try {
            verify = JWT.require(this.hmacKey).build().verify(token);
        }catch (Exception e){
            return Optional.empty();
        }
        Claim uid = verify.getClaim("uid");
        return Optional.of(uid.asInt());
    }
}