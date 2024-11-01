package cn.wzpmc.filemanager.entities.vo;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.util.Date;

@Table("folder")
@Data
public class FolderVo {
    @Id(keyType = KeyType.Auto)
    private int id;
    private String name;
    private int parent;
    private int creator;
    @Column(onInsertValue = "now()")
    private Date createTime;

}