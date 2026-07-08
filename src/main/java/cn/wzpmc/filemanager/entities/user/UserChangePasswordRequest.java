package cn.wzpmc.filemanager.entities.user;

import com.alibaba.fastjson2.annotation.JSONCompiled;
import lombok.Data;

/**
 * 用户修改密码请求
 */
@Data
@JSONCompiled
public class UserChangePasswordRequest {
    /**
     * 旧密码
     */
    private String oldPassword;
    /**
     * 新密码
     */
    private String newPassword;
}
