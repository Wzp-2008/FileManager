package cn.wzpmc.filemanager.commands;

import cn.wzpmc.filemanager.entities.vo.UserVo;
import cn.wzpmc.filemanager.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.shell.core.command.annotation.Command;

@Configuration
public class AuthorizationCommands {
    private final UserService userService;

    @Autowired
    public AuthorizationCommands(UserService userService) {
        this.userService = userService;
    }

    @Command(name = "key", description = "创建一个密钥")
    public void key() {
        this.userService.genInviteCode(UserVo.CONSOLE, "0.0.0.0");
    }
}