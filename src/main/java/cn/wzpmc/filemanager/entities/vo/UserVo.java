package cn.wzpmc.filemanager.entities.vo;

import cn.wzpmc.filemanager.entities.user.enums.Auth;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Data;

@Table("user")
@Data
@AllArgsConstructor
public class UserVo {
    @Id(keyType = KeyType.Auto)
    private long id;
    private String name;
    private String password;
    private Auth auth;
    @Column(isLogicDelete = true, onInsertValue = "0")
    private boolean banned;
    public UserVo(String name, String password, Auth auth) {
        this.name = name;
        this.password = password;
        this.auth = auth;
    }
    private UserVo(long id, String name, Auth auth){
        this.id = id;
        this.name = name;
        this.auth = auth;
    }
    public UserVo(long id) {
        this.id = id;
    }
    public static final UserVo CONSOLE = new UserVo(0L, "CONSOLE", Auth.admin);
}