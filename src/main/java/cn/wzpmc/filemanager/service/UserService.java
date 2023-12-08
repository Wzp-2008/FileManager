package cn.wzpmc.filemanager.service;

import cn.wzpmc.filemanager.entities.Result;
import cn.wzpmc.filemanager.entities.User;
import cn.wzpmc.filemanager.entities.vo.UserLoginVo;
import cn.wzpmc.filemanager.entities.vo.UserRegisterVo;
import cn.wzpmc.filemanager.enums.Auth;
import cn.wzpmc.filemanager.enums.HttpCodes;
import cn.wzpmc.filemanager.mapper.UserMapper;
import cn.wzpmc.filemanager.utils.JwtUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class UserService {
    private final UserMapper mapper;
    private final RedisTemplate<String, String> redisTemplate;
    private final JwtUtils jwtUtils;
    @Autowired
    public UserService(UserMapper mapper, RedisTemplate<String, String> redisTemplate, JwtUtils jwtUtils){
        this.mapper = mapper;
        this.redisTemplate = redisTemplate;
        this.jwtUtils = jwtUtils;
        this.mapper.createDefault();
        if (this.mapper.countUser() == 0) {
            String s = generatorAdminKey();
            log.info("生成随机管理员密钥：{}，有效期：15分钟（若15分钟外未使用，请重启后端重新生成）", s);
        }
    }
    public String generatorAdminKey(){
        String s = JwtUtils.generatorRandomString(8);
        this.redisTemplate.opsForValue().set(s, s, 15, TimeUnit.MINUTES);
        return s;
    }
    public Result<User> login(UserLoginVo loginVo, HttpServletResponse response) {
        loginVo.sha512Password();
        User user = mapper.getUser(loginVo);
        if (user == null){
            return Result.failed(HttpCodes.HTTP_CODES501);
        }
        user.clearPassword();
        response.addHeader("Set-Authorization", jwtUtils.createToken(user));
        return Result.success(user);
    }

    public Result<User> register(UserRegisterVo registerVo, HttpServletResponse response) {
        if (registerVo.isEmptyNamePassword()){
            return Result.failed(HttpCodes.HTTP_CODES401);
        }
        if (registerVo.getAuth().equals(Auth.admin)){
            String verifyCode = registerVo.getVerifyCode();
            String andDelete = this.redisTemplate.opsForValue().getAndDelete(verifyCode);
            if (!Objects.equals(verifyCode, andDelete)){
                return Result.failed(HttpCodes.ACCESS_DENIED);
            }
        }
        if (this.mapper.getUserCountByName(registerVo.getName()) >= 1){
            // 用户已存在
            return Result.failed(HttpCodes.HTTP_CODES501);
        }
        registerVo.sha512Password();
        User user = new User();
        user.setName(registerVo.getName());
        user.setPassword(registerVo.getPassword());
        user.setAuth(registerVo.getAuth());
        this.mapper.addUser(user);
        response.addHeader("Set-Authorization", jwtUtils.createToken(user));
        return Result.success(user);
    }

    public Result<String> generatorVerifyCode(String authorization) {
        Optional<User> optionalUser = jwtUtils.getUser(authorization);
        if (optionalUser.isEmpty()) {
            return Result.failed(HttpCodes.ACCESS_DENIED);
        }
        User user = optionalUser.get();
        int id = user.getId();
        if (Auth.user.equals(this.mapper.getUserAuthById(id))) {
            return Result.failed(HttpCodes.ACCESS_DENIED);
        }
        String s = generatorAdminKey();
        return Result.success(s);
    }
}
