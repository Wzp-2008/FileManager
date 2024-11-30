package cn.wzpmc.filemanager.entities.vo;

import com.alibaba.fastjson2.annotation.JSONCompiled;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.util.Date;

@Table("folder")
@Data
@JSONCompiled
public class FolderVo {
    @Id(keyType = KeyType.Auto)
    private long id;
    private String name;
    private long parent;
    private long creator;
    @Column(onInsertValue = "now()")
    private Date createTime;

}