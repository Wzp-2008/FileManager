package cn.wzpmc.filemanager.service;

import cn.wzpmc.filemanager.config.FileManagerProperties;
import cn.wzpmc.filemanager.entities.Result;
import cn.wzpmc.filemanager.entities.fingerprint.FingerprintRequest;
import cn.wzpmc.filemanager.entities.statistics.enums.Actions;
import cn.wzpmc.filemanager.entities.user.UserChangePasswordRequest;
import cn.wzpmc.filemanager.entities.user.UserLoginRequest;
import cn.wzpmc.filemanager.entities.user.UserRegisterRequest;
import cn.wzpmc.filemanager.entities.user.enums.Auth;
import cn.wzpmc.filemanager.entities.vo.FingerprintVo;
import cn.wzpmc.filemanager.entities.vo.PrefsVo;
import cn.wzpmc.filemanager.entities.vo.UserVo;
import cn.wzpmc.filemanager.mapper.FingerprintMapper;
import cn.wzpmc.filemanager.mapper.PrefsMapper;
import cn.wzpmc.filemanager.mapper.UserMapper;
import cn.wzpmc.filemanager.utils.JwtUtils;
import cn.wzpmc.filemanager.utils.RandomUtils;
import com.alibaba.fastjson2.JSONObject;
import com.mybatisflex.core.query.QueryCondition;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static cn.wzpmc.filemanager.entities.vo.table.FingerprintVoTableDef.FINGERPRINT_VO;
import static cn.wzpmc.filemanager.entities.vo.table.PrefsVoTableDef.PREFS_VO;
import static cn.wzpmc.filemanager.entities.vo.table.UserVoTableDef.USER_VO;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserMapper userMapper;
    private final JwtUtils jwtUtils;
    private final StringRedisTemplate authTemplate;
    private final RandomUtils randomUtils;
    private final StatisticsService statisticsService;
    private final PrefsMapper prefsMapper;
    private final FingerprintMapper fingerprintMapper;
    private final FileManagerProperties properties;
    private static final BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

    /**
     * 若用户表没有用户时生成一个管理员密钥并输出，否则不生成
     */
    public void tryGenFirstAdminKey() {
        // 获取当前用户数量
        long count = this.userMapper.selectCountByQuery(new QueryWrapper());
        if (count == 0) {
            // 若用户数量为0则生成管理员密钥
            String s = genInviteCode(UserVo.CONSOLE, "0.0.0.0");
            log.info("生成了管理员密钥：{}，有效期15分钟，若失效请使用控制台命令/key或重启后端重新生成！", s);
        }
    }

    /**
     * 判断一段字符串是否为MD5字符串
     *
     * @param text 字符串
     * @return 是否为MD5
     */
    private boolean isNotMd5(String text) {
        return text == null || !text.matches("[0-9a-fA-F]{32}");
    }

    public Result<UserVo> login(UserLoginRequest request, HttpServletResponse response, String address) {
        String username = request.getUsername();
        String password = request.getPassword();
        if (this.isNotMd5(password)) {
            return Result.failed(HttpStatus.BAD_REQUEST, "密码需要使用MD5散列哈希");
        }
        String sha1edPassword = DigestUtils.sha1Hex(password);
        QueryCondition findUserCondition = USER_VO.NAME.eq(username);
        UserVo userVo = this.userMapper.selectOneWithRelationsByCondition(findUserCondition);
        if (userVo == null) {
            this.statisticsService.insertAction(Actions.LOGIN, JSONObject.of("status", "error", "msg", "账号或密码错误", "address", address));
            return Result.failed(HttpStatus.UNAUTHORIZED, "账号或密码错误");
        }
        if (!bCryptPasswordEncoder.matches(sha1edPassword, userVo.getPassword())) {
            this.statisticsService.insertAction(Actions.LOGIN, JSONObject.of("status", "error", "msg", "账号或密码错误", "address", address));
            return Result.failed(HttpStatus.UNAUTHORIZED, "账号或密码错误");
        }
        userVo.clearPassword();
        long id = userVo.getId();
        String token = this.jwtUtils.createToken(id);
        response.addHeader("Add-Authorization", token);
        this.statisticsService.insertAction(userVo, Actions.LOGIN, JSONObject.of("status", "success", "address", address));
        return Result.success("登录成功", userVo);
    }

    public Result<UserVo> register(UserRegisterRequest request, HttpServletResponse response, String address) {
        if (properties.isReadonly()) {
            return Result.failed(HttpStatus.LOCKED, "只读模式，不可注册");
        }
        String username = request.getUsername();
        String password = request.getPassword();
        if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
            return Result.failed(HttpStatus.BAD_REQUEST, "用户名/密码不可为空！");
        }
        if (this.isNotMd5(password)) {
            return Result.failed(HttpStatus.BAD_REQUEST, "密码需要使用MD5散列哈希");
        }
        String sha1edPassword = DigestUtils.sha1Hex(password);
        Auth auth = request.getAuth();
        if (this.userMapper.selectCountByCondition(USER_VO.NAME.eq(username)) > 0) {
            this.statisticsService.insertAction(Actions.REGISTER, JSONObject.of("status", "error", "auth", auth, "msg", "用户名已存在", "address", address));
            return Result.failed(HttpStatus.CONFLICT, "用户名已存在，若需要修改密码，请联系网站管理员处理");
        }
        JSONObject statisticsData = JSONObject.of("status", "success", "auth", auth, "address", address);
        if (auth.equals(Auth.admin)) {
            String inviteCode = request.getInviteCode();
            ValueOperations<String, String> ops = authTemplate.opsForValue();
            String andDelete = ops.getAndDelete(inviteCode);
            if (andDelete == null) {
                this.statisticsService.insertAction(Actions.REGISTER, JSONObject.of("status", "error", "auth", auth, "msg", "邀请码错误", "inviteCode", inviteCode, "address", address));
                return Result.failed(HttpStatus.NOT_FOUND, "过期或无效的邀请码");
            }
            statisticsData.put("inviteCode", inviteCode);
        }
        UserVo userVo = new UserVo(username, bCryptPasswordEncoder.encode(sha1edPassword), auth);
        this.userMapper.insert(userVo);
        userVo.clearPassword();
        long id = userVo.getId();
        String token = this.jwtUtils.createToken(id);
        response.addHeader("Add-Authorization", token);
        this.statisticsService.insertAction(userVo, Actions.REGISTER, statisticsData);
        return Result.success("注册成功！", userVo);
    }

    public Result<String> invite(UserVo userVo, String address) {
        if (properties.isReadonly()) return Result.failed(HttpStatus.LOCKED, "只读模式，不可生成");
        String s = genInviteCode(userVo, address);
        return Result.success("生成了一个有效期15分钟的邀请码", s);
    }

    public String genInviteCode(UserVo actor, String address) {
        ValueOperations<String, String> ops = authTemplate.opsForValue();
        String s = this.randomUtils.generatorRandomString(8);
        log.info("生成了新的邀请码：{}", s);
        statisticsService.insertAction(actor, Actions.INVITE, JSONObject.of("remoteAddr", address));
        ops.set(s, "", 15, TimeUnit.MINUTES);
        return s;
    }

    public Result<UserVo> getUserInformation(Long id) {
        UserVo userVo = userMapper.selectOneById(id);
        if (userVo == null) {
            return Result.failed(HttpStatus.NOT_FOUND, "用户不存在！");
        }
        userVo.clearPassword();
        return Result.success(userVo);
    }

    public Result<PrefsVo> updatePrefs(UserVo user, PrefsVo prefs) {
        if (properties.isReadonly()) return Result.failed(HttpStatus.LOCKED, "只读模式，不可保存");
        long id = user.getId();
        if (prefsMapper.selectCountByCondition(PREFS_VO.USER_ID.eq(id)) > 0) {
            prefsMapper.updateByCondition(prefs, false, PREFS_VO.USER_ID.eq(id));
        } else {
            prefs.setUserId(id);
            prefsMapper.insertWithPk(prefs);
        }
        return Result.success(prefs);
    }

    public Result<Boolean> saveFingerprint(UserVo user, FingerprintRequest request, String address) {
        if (properties.isReadonly()) return Result.failed(HttpStatus.LOCKED, "只读模式，不可保存");
        FingerprintVo fingerprintVo = new FingerprintVo();
        fingerprintVo.setUserId(user.getId());
        String fingerprint = request.getFingerprint();
        fingerprintVo.setFingerprint(fingerprint);
        if (fingerprintMapper.selectCountByCondition(FINGERPRINT_VO.FINGERPRINT.eq(fingerprint)) > 0) {
            fingerprintMapper.update(fingerprintVo);
        } else {
            fingerprintMapper.insert(fingerprintVo);
        }
        statisticsService.insertAction(user, Actions.FINGERPRINT_SAVE, JSONObject.of("fingerprint", fingerprint, "address", address));
        return Result.success(true);
    }

    public Result<UserVo> fingerprintLogin(HttpServletResponse response, String fingerprint, String address) {
        FingerprintVo fingerprintVo = fingerprintMapper.selectOneById(fingerprint);
        if (fingerprintVo == null) {
            statisticsService.insertAction(Actions.LOGIN, JSONObject.of("fingerprint", fingerprint, "address", address, "status", "error", "msg", "指纹不存在"));
            return Result.failed(HttpStatus.NOT_FOUND, "指纹不存在！");
        }
        long userId = fingerprintVo.getUserId();
        UserVo userVo = userMapper.selectOneById(userId);
        if (userVo == null) {
            statisticsService.insertAction(Actions.LOGIN, JSONObject.of("fingerprint", fingerprint, "address", address, "status", "error", "msg", "用户不存在或被封禁"));
            return Result.failed(HttpStatus.NOT_FOUND, "用户不存在或被封禁");
        }
        String token = jwtUtils.createToken(userId);
        response.addHeader("Add-Authorization", token);
        statisticsService.insertAction(userVo, Actions.LOGIN, JSONObject.of("fingerprint", fingerprint, "address", address, "status", "success"));
        return Result.success(userVo);
    }

    public Result<Boolean> tryRemoveFingerprint(UserVo user, String fingerprint) {
        if (properties.isReadonly()) return Result.failed(HttpStatus.LOCKED, "只读模式，不可删除");
        int i = fingerprintMapper.deleteByCondition(FINGERPRINT_VO.FINGERPRINT.eq(fingerprint).and(FINGERPRINT_VO.USER_ID.eq(user.getId())));
        if (i > 0) {
            return Result.success("删除成功", true);
        }
        return Result.failed(HttpStatus.NOT_FOUND, "浏览器指纹不存在");
    }

    public Result<Boolean> changePassword(UserChangePasswordRequest request, UserVo userVo) {
        if (properties.isReadonly()) return Result.failed(HttpStatus.LOCKED, "只读模式，不可修改");
        String oldPassword = request.getOldPassword();
        if (this.isNotMd5(oldPassword) || this.isNotMd5(request.getNewPassword())) {
            return Result.failed(HttpStatus.BAD_REQUEST, "新旧密码需要使用MD5散列哈希");
        }
        if (!bCryptPasswordEncoder.matches(DigestUtils.sha1Hex(oldPassword), userVo.getPassword())) {
            return Result.failed(HttpStatus.NOT_FOUND, "旧密码错误！");
        }
        UserVo updateEntity = new UserVo();
        updateEntity.setId(userVo.getId());
        String password = request.getNewPassword();
        updateEntity.setPassword(bCryptPasswordEncoder.encode(DigestUtils.sha1Hex(password)));
        userMapper.update(updateEntity);
        return Result.success(true);
    }

    public Result<Boolean> changeUsername(String newUsername, UserVo userVo) {
        if (properties.isReadonly()) return Result.failed(HttpStatus.LOCKED, "只读模式，不可修改");
        if (userMapper.selectCountByCondition(USER_VO.NAME.eq(newUsername)) >= 1) {
            return Result.failed(HttpStatus.CONFLICT, "用户名已存在！");
        }
        UserVo updateEntity = new UserVo();
        updateEntity.setId(userVo.getId());
        updateEntity.setName(newUsername);
        userMapper.update(updateEntity);
        return Result.success("修改成功", true);
    }

    @Transactional
    public void mergePassword2Bcrypt() {
        log.info("迁移表结构");
        userMapper.mergeUserPassword2Bcrypt();
        log.info("表结构修改完成，开始迁移数据");
        List<UserVo> userVos = userMapper.selectAll();
        if (userVos != null) {
            log.info("共{}条数据需要迁移", userVos.size());
            for (UserVo userVo : userVos) {
                String password = userVo.getPassword();
                String newPassword = bCryptPasswordEncoder.encode(password);
                UserVo updateVo = new UserVo();
                updateVo.setId(userVo.getId());
                updateVo.setPassword(newPassword);
                userMapper.update(updateVo);
            }
        }
        log.info("迁移完成！");
    }
}