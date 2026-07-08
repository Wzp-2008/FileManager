package cn.wzpmc.filemanager.entities.user;

import com.alibaba.fastjson2.annotation.JSONCompiled;
import lombok.Data;

/**
 * 用户登录请求
 */
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