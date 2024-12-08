package cn.wzpmc.filemanager.entities.user;

import cn.wzpmc.filemanager.entities.user.enums.Auth;
import com.alibaba.fastjson2.annotation.JSONCompiled;
import lombok.Data;

@Data
@JSONCompiled
public class UserRegisterRequest {
    /**
     * 用户名
     */
    private String username;
    /**
     * 密码（经过MD5摘要）
     */
    private String password;
    /**
     * 用户类型
     */
    private Auth auth;
    /**
     * 邀请码（当作为admin（管理员）注册时需填写，若作为user（普通用户）注册时无需）
     */
    private String inviteCode;
}