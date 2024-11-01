package cn.wzpmc.filemanager.entities.user.enums;

import com.mybatisflex.annotation.EnumValue;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum Auth {
    admin(1, "admin"), user(0, "user");
    public final int value;
    @EnumValue
    public final String name;
}