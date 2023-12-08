package cn.wzpmc.filemanager.entities.vo;

import cn.wzpmc.filemanager.entities.abs.PasswordObject;
import cn.wzpmc.filemanager.enums.Auth;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Objects;

@EqualsAndHashCode(callSuper = true)
@Data
public class UserRegisterVo extends PasswordObject {
    private String name;
    private Auth auth;
    private String verifyCode;
    private final static String EMPTY_PASSWORD = "d41d8cd98f00b204e9800998ecf8427e";
    public boolean isEmptyNamePassword(){
        return Objects.isNull(name) || Objects.equals(EMPTY_PASSWORD, super.password);
    }
}
