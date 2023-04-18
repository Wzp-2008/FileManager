package cn.wzpmc.filemanager.utils;

import cn.wzpmc.filemanager.entities.User;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Optional;
import java.util.Random;

@Service
@Log4j2
public class JwtUtils {
    private final Algorithm hmacKey;
    private static String generatorHmacKey(){
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            int c = new Random().nextInt(33, 126);
            builder.append((char) c);
        }
        return builder.toString();
    }
    public JwtUtils(@Value("${hmac-key}") String hmacKey){
        String key;
        if ("RANDOM".equalsIgnoreCase(hmacKey)){
            key = generatorHmacKey();
            log.info("Using Random Hmac Key: {}", key);
        }else{
            key = hmacKey;
        }
        this.hmacKey = Algorithm.HMAC512(key);
    }
    public String createToken(User user){
        Calendar instance = Calendar.getInstance();
        instance.add(Calendar.HOUR,24 * 5);
        JWTCreator.Builder builder = JWT.create();
        builder.withClaim("username", user.getUsername());
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
        String username = verify.getClaim("username").asString();
        Integer id = verify.getClaim("id").asInt();
        User user = new User();
        user.setUsername(username);
        user.setId(id);
        return Optional.of(user);
    }
}
