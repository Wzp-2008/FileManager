package cn.wzpmc.filemanager.entities.vo;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Table("file")
@Data
public class FileVo implements Serializable {
    @Id(keyType = KeyType.Auto)
    private long id;
    private String name;
    private String ext;
    private String mime;
    private String hash;
    private long uploader;
    private long folder;
    private long size;
    @Column(onInsertValue = "now()")
    private Date uploadTime;

}