package cn.wzpmc.filemanager.service;

import cn.wzpmc.filemanager.entities.Result;
import cn.wzpmc.filemanager.entities.user.UserLoginRequest;
import cn.wzpmc.filemanager.entities.user.UserRegisterRequest;
import cn.wzpmc.filemanager.entities.user.enums.Auth;
import cn.wzpmc.filemanager.entities.vo.UserVo;
import cn.wzpmc.filemanager.mapper.UserMapper;
import cn.wzpmc.filemanager.utils.JwtUtils;
import cn.wzpmc.filemanager.utils.RandomUtils;
import com.mybatisflex.core.query.QueryCondition;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

import static cn.wzpmc.filemanager.entities.vo.table.UserVoTableDef.USER_VO;

@Slf4j
@Service
public class UserService {
    private final UserMapper userMapper;
    private final JwtUtils jwtUtils;
    private final StringRedisTemplate authTemplate;
    private final RandomUtils randomUtils;
    @Autowired
    public UserService(UserMapper userMapper, JwtUtils jwtUtils, StringRedisTemplate authTemplate, RandomUtils randomUtils) {
        this.userMapper = userMapper;
        this.jwtUtils = jwtUtils;
        this.authTemplate = authTemplate;
        this.randomUtils = randomUtils;
        long count = this.userMapper.selectCountByQuery(new QueryWrapper());
        if (count == 0) {
            String s = genInviteCode();
            log.info("生成了管理员密钥：{}，有效期15分钟，若失效请使用控制台命令/key或重启后端重新生成！", s);
        }
    }
    public void login(UserLoginRequest request, HttpServletResponse response) {
        String username = request.getUsername();
        String password = request.getPassword();
        String sha1edPassword = DigestUtils.sha1Hex(password);
        QueryCondition findUserCondition = USER_VO.NAME.eq(username).and(USER_VO.PASSWORD.eq(sha1edPassword));
        long count = this.userMapper.selectCountByCondition(findUserCondition);
        if (count < 0) {
            Result.failed(HttpStatus.UNAUTHORIZED, "账号或密码错误").writeToResponse(response);
            return;
        }
        UserVo userVo = this.userMapper.selectOneByCondition(findUserCondition);
        String token = this.jwtUtils.createToken(userVo.getId());
        response.addHeader("Add-Authorization", token);
        Result.success("登录成功").writeToResponse(response);
    }
    public void register(UserRegisterRequest request, HttpServletResponse response) {
        String username = request.getUsername();
        String password = request.getPassword();
        Auth auth = request.getAuth();
        if (this.userMapper.selectCountByCondition(USER_VO.NAME.eq(username)) > 0) {
            Result.failed(HttpStatus.CONFLICT).msg("用户名已存在，若需要修改密码，请联系网站管理员处理").writeToResponse(response);
            return;
        }
        if (auth.equals(Auth.admin)) {
            String inviteCode = request.getInviteCode();
            ValueOperations<String, String> ops = authTemplate.opsForValue();
            String andDelete = ops.getAndDelete(inviteCode);
            if (andDelete == null) {
                Result.failed(HttpStatus.NOT_FOUND, "过期或无效的邀请码").writeToResponse(response);
                return;
            }
        }
        UserVo userVo = new UserVo(username, password, auth);
        this.userMapper.insert(userVo);
        int id = userVo.getId();
        String token = this.jwtUtils.createToken(id);
        response.addHeader("Add-Authorization", token);
        Result.success("注册成功！").writeToResponse(response);
    }

    public Result<String> invite() {
        String s = genInviteCode();
        return Result.success("生成了一个有效期15分钟的邀请码", s);
    }
    public String genInviteCode() {
        ValueOperations<String, String> ops = authTemplate.opsForValue();
        String s = this.randomUtils.generatorRandomString(8);
        log.info("生成了新的邀请码：{}", s);
        ops.set(s, "", 15, TimeUnit.MINUTES);
        return s;
    }
}