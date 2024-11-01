package cn.wzpmc.filemanager.entities.vo;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.util.Date;
import java.util.Objects;

@Table("file")
@Data
public class FileVo {
    @Id(keyType = KeyType.Auto)
    private int id;
    private String name;
    private String ext;
    private String mime;
    private String sha1;
    private int uploader;
    private int folder;
    @Column(onInsertValue = "now()")
    private Date uploadTime;

}