package cn.wzpmc.filemanager.controller;

import cn.wzpmc.filemanager.entities.ResponseResult;
import cn.wzpmc.filemanager.entities.User;
import cn.wzpmc.filemanager.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
public class UserController {
    private final UserService service;
    @Autowired
    public UserController(UserService service){
        this.service = service;
    }
    @PostMapping("/api/user/login")
    public ResponseResult<User> login(@RequestBody User user, HttpServletRequest request, HttpServletResponse response){
        return new ResponseResult<>(this.service.login(user, request.getRemoteAddr(), response));
    }
    @PostMapping("/api/user/register")
    public User register(@RequestBody User user, HttpServletRequest request, HttpServletResponse response){
        return this.service.register(user, request.getRemoteAddr(), response);
    }
}
