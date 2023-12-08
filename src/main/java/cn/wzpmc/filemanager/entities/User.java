package cn.wzpmc.filemanager.entities;

import cn.wzpmc.filemanager.entities.abs.PasswordObject;
import cn.wzpmc.filemanager.enums.Auth;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.codec.digest.DigestUtils;

@EqualsAndHashCode(callSuper = true)
@Data
public class User extends PasswordObject {
    private int id;
    private String name;
    private Auth auth;
}
