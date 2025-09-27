package cn.wzpmc.filemanager.entities;

import cn.wzpmc.filemanager.entities.abs.PasswordObject;
import cn.wzpmc.filemanager.enums.Auth;
import com.alibaba.fastjson2.annotation.JSONCompiled;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@JSONCompiled
public class User extends PasswordObject {
    private int id;
    private String name;
    private Auth auth;
}