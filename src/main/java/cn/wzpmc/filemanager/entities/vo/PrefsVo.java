package cn.wzpmc.filemanager.entities.vo;

import cn.wzpmc.filemanager.entities.files.enums.SortField;
import com.alibaba.fastjson2.annotation.JSONCompiled;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.io.Serializable;

@Table("prefs")
@Data
@JSONCompiled
public class PrefsVo implements Serializable {
    /**
     * 用户ID
     */
    @Id(keyType = KeyType.Auto)
    @Column("user_id")
    private Long userId;
    /**
     * 排序方式
     */
    @Column("sort_field")
    private SortField sortField;
    /**
     * 反向排序
     */
    @Column("sort_reverse")
    private boolean sortReverse;
}
