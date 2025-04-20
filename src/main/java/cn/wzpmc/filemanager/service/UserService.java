package cn.wzpmc.filemanager.service;

import cn.wzpmc.filemanager.entities.Result;
import cn.wzpmc.filemanager.entities.statistics.enums.Actions;
import cn.wzpmc.filemanager.entities.user.UserLoginRequest;
import cn.wzpmc.filemanager.entities.user.UserRegisterRequest;
import cn.wzpmc.filemanager.entities.user.enums.Auth;
import cn.wzpmc.filemanager.entities.vo.UserVo;
import cn.wzpmc.filemanager.mapper.UserMapper;
import cn.wzpmc.filemanager.utils.JwtUtils;
import cn.wzpmc.filemanager.utils.RandomUtils;
import com.alibaba.fastjson2.JSONObject;
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import static cn.wzpmc.filemanager.entities.vo.table.UserVoTableDef.USER_VO;

@Slf4j
@Service
public class UserService {
    private final UserMapper userMapper;
    private final JwtUtils jwtUtils;
    private final StringRedisTemplate authTemplate;
    private final RandomUtils randomUtils;
    private final StatisticsService statisticsService;
    @Autowired
    public UserService(UserMapper userMapper, JwtUtils jwtUtils, StringRedisTemplate authTemplate, RandomUtils randomUtils, StatisticsService statisticsService) {
        this.userMapper = userMapper;
        this.jwtUtils = jwtUtils;
        this.authTemplate = authTemplate;
        this.randomUtils = randomUtils;
        this.statisticsService = statisticsService;
        long count = this.userMapper.selectCountByQuery(new QueryWrapper());
        if (count == 0) {
            String s = genInviteCode(UserVo.CONSOLE, "0.0.0.0");
            log.info("生成了管理员密钥：{}，有效期15分钟，若失效请使用控制台命令/key或重启后端重新生成！", s);
        }
    }
    public void login(UserLoginRequest request, HttpServletResponse response, String address) {
        String username = request.getUsername();
        String password = request.getPassword();
        String sha1edPassword = DigestUtils.sha1Hex(password);
        QueryCondition findUserCondition = USER_VO.NAME.eq(username).and(USER_VO.PASSWORD.eq(sha1edPassword));
        long count = this.userMapper.selectCountByCondition(findUserCondition);
        if (count <= 0) {
            this.statisticsService.insertAction(Actions.LOGIN, JSONObject.of("status", "error", "msg", "账号或密码错误", "address", address));
            Result.failed(HttpStatus.UNAUTHORIZED, "账号或密码错误").writeToResponse(response);
            return;
        }
        UserVo userVo = this.userMapper.selectOneByCondition(findUserCondition);
        userVo.clearPassword();
        long id = userVo.getId();
        String token = this.jwtUtils.createToken(id);
        response.addHeader("Add-Authorization", token);
        this.statisticsService.insertAction(userVo, Actions.LOGIN, JSONObject.of("status", "success", "address", address));
        Result.success("登录成功", userVo).writeToResponse(response);
    }
    public void register(UserRegisterRequest request, HttpServletResponse response, String address) {
        String username = request.getUsername();
        String password = request.getPassword();
        String sha1edPassword = DigestUtils.sha1Hex(password);
        Auth auth = request.getAuth();
        if (this.userMapper.selectCountByCondition(USER_VO.NAME.eq(username)) > 0) {
            this.statisticsService.insertAction(Actions.REGISTER, JSONObject.of("status", "error", "auth", auth, "msg", "用户名已存在", "address", address));
            Result.failed(HttpStatus.CONFLICT).msg("用户名已存在，若需要修改密码，请联系网站管理员处理").writeToResponse(response);
            return;
        }
        JSONObject statisticsData = JSONObject.of("status", "success", "auth", auth, "address", address);
        if (auth.equals(Auth.admin)) {
            String inviteCode = request.getInviteCode();
            ValueOperations<String, String> ops = authTemplate.opsForValue();
            String andDelete = ops.getAndDelete(inviteCode);
            if (andDelete == null) {
                this.statisticsService.insertAction(Actions.REGISTER, JSONObject.of("status", "error", "auth", auth, "msg", "邀请码错误", "inviteCode", inviteCode, "address", address));
                Result.failed(HttpStatus.NOT_FOUND, "过期或无效的邀请码").writeToResponse(response);
                return;
            }
            statisticsData.put("inviteCode", inviteCode);
        }
        UserVo userVo = new UserVo(username, sha1edPassword, auth);
        this.userMapper.insert(userVo);
        userVo.clearPassword();
        long id = userVo.getId();
        String token = this.jwtUtils.createToken(id);
        response.addHeader("Add-Authorization", token);
        this.statisticsService.insertAction(userVo, Actions.REGISTER, statisticsData);
        Result.success("注册成功！", userVo).writeToResponse(response);
    }

    public Result<String> invite(UserVo userVo, String address) {
        String s = genInviteCode(userVo, address);
        return Result.success("生成了一个有效期15分钟的邀请码", s);
    }
    public String genInviteCode(UserVo actor, String address) {
        ValueOperations<String, String> ops = authTemplate.opsForValue();
        String s = this.randomUtils.generatorRandomString(8);
        log.info("生成了新的邀请码：{}", s);
        statisticsService.insertAction(actor, Actions.INVITE, address);
        ops.set(s, "", 15, TimeUnit.MINUTES);
        return s;
    }

    public Result<UserVo> getUserInformation(Long id) {
        UserVo userVo = userMapper.selectOneById(id);
        if (userVo == null){
            return Result.failed(HttpStatus.NOT_FOUND, "用户不存在！");
        }
        userVo.clearPassword();
        return Result.success(userVo);
    }
}