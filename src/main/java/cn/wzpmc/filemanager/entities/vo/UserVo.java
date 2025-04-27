package cn.wzpmc.filemanager.entities.vo;

import cn.wzpmc.filemanager.entities.user.enums.Auth;
import com.alibaba.fastjson2.annotation.JSONCompiled;
import com.mybatisflex.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table("user")
@Data
@AllArgsConstructor
@NoArgsConstructor
@JSONCompiled
public class UserVo {
    /**
     * 用户ID
     */
    @Id(keyType = KeyType.Auto)
    private long id;
    /**
     * 用户名
     */
    private String name;
    /**
     * 用户密码
     * @ignore
     */
    private String password;
    /**
     * 用户类型
     */
    private Auth auth;
    /**
     * 用户是否被封禁
     * `尽管封了你也看不到就是了`
     */
    @Column(isLogicDelete = true, onInsertValue = "0")
    private boolean banned;

    @RelationOneToOne(selfField = "id", targetField = "userId", targetTable = "prefs")
    private PrefsVo prefs;

    public UserVo(String name, String password, Auth auth) {
        this.name = name;
        this.password = password;
        this.auth = auth;
    }

    private UserVo(long id, String name, Auth auth) {
        this.id = id;
        this.name = name;
        this.auth = auth;
    }

    public UserVo(long id) {
        this.id = id;
    }

    public void clearPassword() {
        this.setPassword(null);
    }

    public static final UserVo CONSOLE = new UserVo(0L, "CONSOLE", Auth.admin);
}