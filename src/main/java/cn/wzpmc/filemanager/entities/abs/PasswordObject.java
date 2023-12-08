package cn.wzpmc.filemanager.entities.abs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;

@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class PasswordObject {
    protected String password;
    public void clearPassword(){
        this.setPassword(null);
    }
    public void sha512Password(){
        this.setPassword(DigestUtils.sha512Hex(this.getPassword()));
    }
}
