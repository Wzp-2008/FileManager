package cn.wzpmc.filemanager.utils;

import cn.wzpmc.filemanager.entities.User;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Optional;
import java.util.Random;

@Service
@Slf4j
public class JwtUtils {
    private final Algorithm hmacKey;
    private static String generatorRandomCharList(int size, char minChar, char maxChar){
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < size; i++) {
            int c = new Random().nextInt(minChar, maxChar);
            builder.append((char) c);
        }
        return builder.toString();
    }
    public static String generatorRandomLowerStandString(int size){
        return generatorRandomCharList(size, 'a', 'z');
    }
    public static String generatorRandomString(int size){
        return generatorRandomCharList(size, '!', '~');
    }
    private static String generatorHmacKey(){
        return generatorRandomString(16);
    }
    public JwtUtils(@Value("${hmac-key}") String hmacKey){
        String key;
        if ("RANDOM".equalsIgnoreCase(hmacKey)){
            key = generatorHmacKey();
            log.info("使用随机的HMAC密钥：{}", key);
        }else{
            key = hmacKey;
        }
        this.hmacKey = Algorithm.HMAC512(key);
    }
    public String createToken(User user){
        Calendar instance = Calendar.getInstance();
        instance.add(Calendar.HOUR,24 * 5);
        JWTCreator.Builder builder = JWT.create();
        builder.withClaim("name", user.getName());
        builder.withClaim("id", user.getId());
        builder.withExpiresAt(instance.getTime());
        return builder.sign(this.hmacKey);
    }
    public Optional<User> getUser(String token){
        DecodedJWT verify;
        try {
            verify = JWT.require(this.hmacKey).build().verify(token);
        }catch (Exception e){
            return Optional.empty();
        }
        String username = verify.getClaim("name").asString();
        Integer id = verify.getClaim("id").asInt();
        User user = new User();
        user.setName(username);
        user.setId(id);
        return Optional.of(user);
    }
}