package cn.wzpmc.filemanager.entities.user;

import com.alibaba.fastjson2.annotation.JSONCompiled;
import lombok.Data;

/**
 * @since 2025/6/29 21:42
 * @author wzp
 * @version 1.0.0
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
