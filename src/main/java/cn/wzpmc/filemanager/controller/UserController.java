package cn.wzpmc.filemanager.controller;

import cn.wzpmc.filemanager.entities.Result;
import cn.wzpmc.filemanager.entities.User;
import cn.wzpmc.filemanager.entities.vo.UserLoginVo;
import cn.wzpmc.filemanager.entities.vo.UserRegisterVo;
import cn.wzpmc.filemanager.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private final UserService service;
    @Autowired
    public UserController(UserService service){
        this.service = service;
    }
    @PostMapping("/login")
    public Result<User> login(@RequestBody UserLoginVo loginVo, HttpServletResponse response){
        return service.login(loginVo, response);
    }
    @PostMapping("/register")
    public Result<User> register(@RequestBody UserRegisterVo registerVo, HttpServletResponse response){
        return service.register(registerVo, response);
    }
    @GetMapping("/verifyCode")
    public Result<String> generatorVerifyCode(@RequestHeader("Authorization") String authorization){
        return service.generatorVerifyCode(authorization);
    }
}
