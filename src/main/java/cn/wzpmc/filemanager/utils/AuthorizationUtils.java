package cn.wzpmc.filemanager.utils;

import cn.wzpmc.filemanager.annotation.AuthorizationRequired;
import cn.wzpmc.filemanager.entities.Result;
import cn.wzpmc.filemanager.entities.user.enums.Auth;
import cn.wzpmc.filemanager.entities.vo.UserVo;
import cn.wzpmc.filemanager.exceptions.AuthorizationException;
import cn.wzpmc.filemanager.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.WebRequest;

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
            throw new AuthorizationException(Result.failed(HttpStatus.UNAUTHORIZED, "token错误或已过期"));
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
    public UserVo auth(WebRequest request, AuthorizationRequired authorizationRequired) throws AuthorizationException {
        return auth(request.getHeader("Authorization"), authorizationRequired);
    }
    public boolean auth(HttpServletRequest request, AuthorizationRequired authorizationRequired) throws AuthorizationException {
        auth(request.getHeader("Authorization"), authorizationRequired);
        return true;
    }
}