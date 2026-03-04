package cn.wzpmc.filemanager.entities.user.enums;

import cn.wzpmc.filemanager.mybatis.PgEnumName;
import com.alibaba.fastjson2.annotation.JSONCompiled;
import com.mybatisflex.annotation.EnumValue;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@JSONCompiled
@PgEnumName("user_auth")
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