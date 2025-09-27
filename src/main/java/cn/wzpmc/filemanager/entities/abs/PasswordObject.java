package cn.wzpmc.filemanager.entities.abs;

import com.alibaba.fastjson2.annotation.JSONCompiled;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JSONCompiled
public abstract class PasswordObject {
    protected String password;
    public void clearPassword(){
        this.setPassword(null);
    }
    public void sha512Password(){
        this.setPassword(DigestUtils.sha512Hex(this.getPassword()));
    }
}