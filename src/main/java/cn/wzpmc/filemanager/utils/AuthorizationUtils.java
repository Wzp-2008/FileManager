package cn.wzpmc.filemanager.utils;

import cn.wzpmc.filemanager.annotation.AuthorizationRequired;
import cn.wzpmc.filemanager.entities.Result;
import cn.wzpmc.filemanager.entities.user.enums.Auth;
import cn.wzpmc.filemanager.entities.vo.UserVo;
import cn.wzpmc.filemanager.exceptions.AuthorizationException;
import cn.wzpmc.filemanager.exceptions.TokenExpireAuthorizationException;
import cn.wzpmc.filemanager.mapper.UserMapper;
import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.NativeWebRequest;

import java.util.Date;
import java.util.Optional;

@Slf4j
@Component
public class AuthorizationUtils {
    private final JwtUtils jwtUtils;
    private final UserMapper userMapper;
    @Autowired
    public AuthorizationUtils(JwtUtils jwtUtils, UserMapper userMapper) {
        this.jwtUtils = jwtUtils;
        this.userMapper = userMapper;
    }
    private UserVo auth(String header, AuthorizationRequired authorizationRequired) throws AuthorizationException {
        log.info("auth {} with token {}", authorizationRequired, header);
        if (header == null) {
            throw new AuthorizationException(Result.failed(HttpStatus.UNAUTHORIZED, "未找到token"));
        }
        Auth level = authorizationRequired.level();
        Optional<Integer> user = this.jwtUtils.getUser(header);
        if (user.isEmpty()) {
            throw TokenExpireAuthorizationException.of();
        }
        Integer i = user.get();
        UserVo userVo = this.userMapper.selectOneWithRelationsById(i);
        if (userVo == null) {
            throw new AuthorizationException(Result.failed(HttpStatus.UNAUTHORIZED, "用户不存在"));
        }
        Auth auth = userVo.getAuth();
        if (authorizationRequired.force()) {
            if (auth.value == level.value) {
                return userVo;
            }
        }else {
            if (auth.value >= level.value) {
                return userVo;
            }
        }
        throw new AuthorizationException(Result.failed(HttpStatus.UNAUTHORIZED, "权限不足"));
    }

    private UserVo tryGetUserFromExpired(String originalToken) {
        DecodedJWT decode = JWT.decode(originalToken);
        try {
            Date expiresAt = decode.getExpiresAt();
            JWT.require(jwtUtils.getHmacKey()).acceptExpiresAt(expiresAt.getTime()).build().verify(decode);
        } catch (JWTVerificationException e) {
            throw new AuthorizationException(Result.failed(HttpStatus.UNAUTHORIZED, "签名不正确"));
        }
        Claim uid = decode.getClaim("uid");
        Long anInt = uid.asLong();
        UserVo userVo = this.userMapper.selectOneWithRelationsById(anInt);
        if (userVo == null) {
            throw new AuthorizationException(Result.failed(HttpStatus.UNAUTHORIZED, "用户不存在"));
        }
        return userVo;
    }

    public UserVo auth(NativeWebRequest request, AuthorizationRequired authorizationRequired) throws AuthorizationException {
        String authorization = request.getHeader("Authorization");
        if (authorization == null) {
            throw new AuthorizationException(Result.failed(HttpStatus.UNAUTHORIZED, "未找到token"));
        }
        try {
            return auth(authorization, authorizationRequired);
        } catch (TokenExpireAuthorizationException ignored) {
            UserVo user = tryGetUserFromExpired(authorization);
            Object nativeResponse = request.getNativeResponse();
            if (nativeResponse instanceof HttpServletResponse resp) {
                resp.addHeader("Add-Authorization", jwtUtils.createToken(user.getId()));
            }
            return user;
        }
    }

    public boolean auth(HttpServletRequest request, HttpServletResponse response, AuthorizationRequired authorizationRequired) throws AuthorizationException {
        String authorization = request.getHeader("Authorization");
        if (authorization == null) {
            throw new AuthorizationException(Result.failed(HttpStatus.UNAUTHORIZED, "未找到token"));
        }
        try {
            auth(authorization, authorizationRequired);
            return true;
        } catch (TokenExpireAuthorizationException ignored) {
            UserVo user = tryGetUserFromExpired(authorization);
            response.addHeader("Add-Authorization", jwtUtils.createToken(user.getId()));
            return true;
        }
    }
}