package cn.wzpmc.filemanager.controller;

import cn.wzpmc.filemanager.annotation.AuthorizationRequired;
import cn.wzpmc.filemanager.entities.Result;
import cn.wzpmc.filemanager.entities.user.UserLoginRequest;
import cn.wzpmc.filemanager.entities.user.UserRegisterRequest;
import cn.wzpmc.filemanager.entities.user.enums.Auth;
import cn.wzpmc.filemanager.entities.vo.UserVo;
import cn.wzpmc.filemanager.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;
    @Autowired
    public UserController(UserService userService) {
        this.userService=  userService;
    }
    @PostMapping("/login")
    public void login(@RequestBody UserLoginRequest loginRequest, HttpServletResponse response) {
        userService.login(loginRequest, response);
    }
    @PutMapping("/register")
    public void register(@RequestBody UserRegisterRequest registerRequest, HttpServletResponse response) {
        userService.register(registerRequest, response);
    }
    @GetMapping("/invite")
    @AuthorizationRequired(level = Auth.admin)
    public Result<String> invite(){
        return userService.invite();
    }
}