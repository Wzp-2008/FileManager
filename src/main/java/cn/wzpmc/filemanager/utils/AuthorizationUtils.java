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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.NativeWebRequest;

import java.util.Date;
import java.util.Optional;

/**
 * 身份认证相关工具类
 *
 * @see JwtUtils
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthorizationUtils {
    private final JwtUtils jwtUtils;
    private final UserMapper userMapper;

    /**
     * 根据Authorization头的值获取用户对象
     *
     * @param header                Authorization头
     * @param authorizationRequired 对应注解
     * @return 用户对象
     * @throws AuthorizationException 当认证失败时抛出
     */
    private UserVo auth(String header, AuthorizationRequired authorizationRequired) throws AuthorizationException {
        log.info("auth {} with token {}", authorizationRequired, header);
        // 若传入的header为空则直接抛出未找到token
        if (header == null) {
            throw new AuthorizationException(Result.failed(HttpStatus.UNAUTHORIZED, "未找到token"));
        }
        // 获取注解所需要的用户等级
        Auth level = authorizationRequired.level();
        // 根据token获取对应用户ID
        Optional<Integer> user = this.jwtUtils.getUser(header);
        // 若找不到对应的用户则判断token已过期
        if (user.isEmpty()) {
            throw TokenExpireAuthorizationException.of();
        }
        // 获取用户ID并查询对应的用户对象
        Integer i = user.get();
        UserVo userVo = this.userMapper.selectOneWithRelationsById(i);
        // 若找不到则判断用户被删除
        if (userVo == null) {
            throw new AuthorizationException(Result.failed(HttpStatus.UNAUTHORIZED, "用户不存在"));
        }
        // 获取用户的类型
        Auth auth = userVo.getAuth();
        // 若要求强制等级则必须一致
        if (authorizationRequired.force()) {
            if (auth.value == level.value) {
                return userVo;
            }
        } else {
            // 否则比要求的等级高即可
            if (auth.value >= level.value) {
                return userVo;
            }
        }
        // 如果条件不满则则抛出权限不足错误
        throw new AuthorizationException(Result.failed(HttpStatus.UNAUTHORIZED, "权限不足"));
    }

    /**
     * 从已经过期了的token中提取用户信息
     *
     * @param originalToken token
     * @return 对应的用户信息
     */
    private UserVo tryGetUserFromExpired(String originalToken) {
        // 跳过JwtUtils，直接使用JWT类解码对应的token信息
        DecodedJWT decode = JWT.decode(originalToken);
        try {
            Date expiresAt = decode.getExpiresAt();
            // 允许过期但需要验证jwt签名正确
            JWT.require(jwtUtils.getHmacKey()).acceptExpiresAt(expiresAt.getTime()).build().verify(decode);
        } catch (JWTVerificationException e) {
            // 若签名不正确直接报错
            throw new AuthorizationException(Result.failed(HttpStatus.UNAUTHORIZED, "签名不正确"));
        }
        // 从被解码的信息中直接获取uid属性
        Claim uid = decode.getClaim("uid");
        Long anInt = uid.asLong();
        // 在用户表中查询对应的用户
        UserVo userVo = this.userMapper.selectOneWithRelationsById(anInt);
        // 用户为null则判断用户被删除
        if (userVo == null) {
            throw new AuthorizationException(Result.failed(HttpStatus.UNAUTHORIZED, "用户不存在"));
        }
        return userVo;
    }

    /**
     * 从NativeWebRequest获取对应authorization头的值并获取用户信息
     *
     * @param request               NativeWebRequest请求
     * @param authorizationRequired 对应注解
     * @return 用户信息
     * @throws AuthorizationException 当认证失败时抛出
     * @see AuthorizationUtils#auth(String, AuthorizationRequired)
     */
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

    /**
     * 从HttpServletRequest获取对应authorization头的值并检查用户是否登录
     *
     * @param request               HttpServletRequest 请求
     * @param authorizationRequired 对应注解
     * @return 用户是否登录
     * @throws AuthorizationException 当认证失败时抛出
     * @see AuthorizationUtils#auth(String, AuthorizationRequired)
     */
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