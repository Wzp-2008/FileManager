package cn.wzpmc.filemanager.controller;

import cn.wzpmc.filemanager.annotation.Address;
import cn.wzpmc.filemanager.annotation.AuthorizationRequired;
import cn.wzpmc.filemanager.entities.Result;
import cn.wzpmc.filemanager.entities.user.UserLoginRequest;
import cn.wzpmc.filemanager.entities.user.UserRegisterRequest;
import cn.wzpmc.filemanager.entities.user.enums.Auth;
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
}