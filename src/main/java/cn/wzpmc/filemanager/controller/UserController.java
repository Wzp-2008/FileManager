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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public void login(@RequestBody UserLoginRequest loginRequest, HttpServletResponse response, @Address String address) {
        userService.login(loginRequest, response, address);
    }

    @PutMapping("/register")
    public void register(@RequestBody UserRegisterRequest registerRequest, HttpServletResponse response, @Address String address) {
        userService.register(registerRequest, response, address);
    }

    @GetMapping("/invite")
    public Result<String> invite(@AuthorizationRequired(level = Auth.admin) UserVo userVo, @Address String address) {
        return userService.invite(userVo, address);
    }
}