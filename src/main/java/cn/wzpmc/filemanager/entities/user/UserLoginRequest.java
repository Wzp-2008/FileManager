package cn.wzpmc.filemanager.entities.user;

import lombok.Data;

@Data
public class UserLoginRequest {
    private String username;
    private String password;
}