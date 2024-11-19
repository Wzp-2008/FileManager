package cn.wzpmc.filemanager.commands;

import cn.wzpmc.filemanager.entities.vo.UserVo;
import cn.wzpmc.filemanager.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

@ShellComponent
public class AuthorizationCommands {
    private final UserService userService;
    @Autowired
    public AuthorizationCommands(UserService userService) {
        this.userService = userService;
    }
    @ShellMethod("创建一个密钥")
    public void key(){
        this.userService.genInviteCode(UserVo.CONSOLE, "0.0.0.0");
    }
}