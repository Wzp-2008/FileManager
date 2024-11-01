package cn.wzpmc.filemanager.entities.user;

import cn.wzpmc.filemanager.entities.user.enums.Auth;
import lombok.Data;

@Data
public class UserRegisterRequest {
    private String username;
    private String password;
    private Auth auth;
    private String inviteCode;
}