package cn.wzpmc.filemanager.entities.user;

import cn.wzpmc.filemanager.entities.user.enums.Auth;
import com.alibaba.fastjson2.annotation.JSONCompiled;
import lombok.Data;

@Data
@JSONCompiled
public class UserRegisterRequest {
    private String username;
    private String password;
    private Auth auth;
    private String inviteCode;
}