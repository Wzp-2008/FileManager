package cn.wzpmc.filemanager.entities.user;

import com.alibaba.fastjson2.annotation.JSONCompiled;
import lombok.Data;

@Data
@JSONCompiled
public class UserLoginRequest {
    private String username;
    private String password;
}