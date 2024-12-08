package cn.wzpmc.filemanager.entities.user.enums;

import com.alibaba.fastjson2.annotation.JSONCompiled;
import com.mybatisflex.annotation.EnumValue;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@JSONCompiled
public enum Auth {
    /**
     * 管理员
     */
    admin(1, "admin"),
    /**
     * 普通用户
     */
    user(0, "user");
    public final int value;
    @EnumValue
    public final String name;
}