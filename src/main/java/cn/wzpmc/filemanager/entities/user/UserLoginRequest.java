package cn.wzpmc.filemanager.entities.user;

import com.alibaba.fastjson2.annotation.JSONCompiled;
import lombok.Data;

@Data
@JSONCompiled
public class UserLoginRequest {
    /**
     * 用户名
     */
    private String username;
    /**
     * 密码（通过MD5摘要）
     */
    private String password;
}