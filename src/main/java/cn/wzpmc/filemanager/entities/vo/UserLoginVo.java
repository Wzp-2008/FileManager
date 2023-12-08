package cn.wzpmc.filemanager.entities.vo;

import cn.wzpmc.filemanager.entities.abs.PasswordObject;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class UserLoginVo extends PasswordObject {
    private String name;
}
