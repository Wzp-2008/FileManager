package cn.wzpmc.filemanager.controller;

import cn.wzpmc.filemanager.annotation.Address;
import cn.wzpmc.filemanager.annotation.AuthorizationRequired;
import cn.wzpmc.filemanager.entities.Result;
import cn.wzpmc.filemanager.entities.fingerprint.FingerprintRequest;
import cn.wzpmc.filemanager.entities.user.UserChangePasswordRequest;
import cn.wzpmc.filemanager.entities.user.UserLoginRequest;
import cn.wzpmc.filemanager.entities.user.UserRegisterRequest;
import cn.wzpmc.filemanager.entities.user.enums.Auth;
import cn.wzpmc.filemanager.entities.vo.PrefsVo;
import cn.wzpmc.filemanager.entities.vo.UserVo;
import cn.wzpmc.filemanager.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 用户相关接口
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    /**
     * 登录接口
     * @param loginRequest 登录请求体
     * @response {@link Result<UserVo>}
     */
    @PostMapping("/login")
    public void login(@RequestBody UserLoginRequest loginRequest, HttpServletResponse response, @Address String address) {
        userService.login(loginRequest, response, address);
    }

    /**
     * 注册接口
     * @param registerRequest 注册请求体
     * @response {@link Result<UserVo>}
     */
    @PutMapping("/register")
    public void register(@RequestBody UserRegisterRequest registerRequest, HttpServletResponse response, @Address String address) {
        userService.register(registerRequest, response, address);
    }

    /**
     * 修改用户密码
     * @param request 用户密码修改请求体
     * @return 是否修改成功
     */
    @PostMapping("/password")
    public Result<Boolean> changePassword(@RequestBody UserChangePasswordRequest request, @AuthorizationRequired UserVo userVo) {
        return userService.changePassword(request, userVo);
    }

    /**
     * 修改用户名
     * @param newUsername 新的用户名
     * @return 是否修改成功
     */
    @PostMapping("/username/{newUsername}")
    public Result<Boolean> changeUsername(@PathVariable String newUsername, @AuthorizationRequired UserVo userVo) {
        return userService.changeUsername(newUsername, userVo);
    }

    /**
     * 管理员用户生成邀请码接口
     * @return 邀请码
     */
    @GetMapping("/invite")
    public Result<String> invite(@AuthorizationRequired(level = Auth.admin) UserVo userVo, @Address String address) {
        return userService.invite(userVo, address);
    }

    /**
     * 获取用户信息接口
     * @return 用户信息
     */
    @GetMapping("/info")
    public Result<UserVo> getUserInfo(@AuthorizationRequired UserVo user) {
        return Result.success(user);
    }

    /**
     * 获取指定用户信息接口
     * @param id 用户ID
     * @return 该ID的用户信息
     */
    @GetMapping("/info/{id}")
    public Result<UserVo> getUser(@PathVariable Long id) {
        return userService.getUserInformation(id);
    }

    /**
     * 修改当前用户设置
     * @param prefs 新的用户设置
     * @return 该用户的新设置
     */
    @PostMapping("/prefs")
    public Result<PrefsVo> updatePrefs(@AuthorizationRequired UserVo user, @RequestBody PrefsVo prefs) {
        return userService.updatePrefs(user, prefs);
    }

    /**
     * 保存浏览器指纹
     * @param request 指纹保存请求体
     * @param address 请求的地址
     * @return 是否保存成功
     */
    @PostMapping("/fingerprint/save")
    public Result<Boolean> saveFingerprint(@AuthorizationRequired UserVo user, @RequestBody FingerprintRequest request, @Address String address) {
        return userService.saveFingerprint(user, request, address);
    }

    /**
     * 尝试使用浏览器指纹登录
     * @param fingerprint 浏览器指纹
     * @param address 请求的地址
     * @return 登录后的用户
     */
    @GetMapping("/fingerprint/login")
    public Result<UserVo> fingerprintLogin(HttpServletResponse response, @RequestParam("fingerprint") String fingerprint, @Address String address) {
        return userService.fingerprintLogin(response, fingerprint, address);
    }

    /**
     * 尝试删除浏览器指纹
     * @param user 用户信息
     * @return 是否删除成功
     */
    @DeleteMapping("/fingerprint/tryRemove")
    public Result<Boolean> tryRemoveFingerprint(@AuthorizationRequired UserVo user, @RequestParam("fingerprint") String fingerprint) {
        return userService.tryRemoveFingerprint(user, fingerprint);
    }
}