package cn.wzpmc.filemanager.entities.vo;

import cn.wzpmc.filemanager.entities.abs.PasswordObject;
import com.alibaba.fastjson2.annotation.JSONCompiled;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@JSONCompiled
public class UserLoginVo extends PasswordObject {
    private String name;
}