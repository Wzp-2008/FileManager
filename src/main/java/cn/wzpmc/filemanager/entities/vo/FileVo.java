package cn.wzpmc.filemanager.entities.vo;

import com.alibaba.fastjson2.annotation.JSONCompiled;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Table("file")
@Data
@JSONCompiled
public class FileVo implements Serializable {
    /**
     * 文件ID
     */
    @Id(keyType = KeyType.Auto)
    private long id;
    /**
     * 文件名
     */
    private String name;
    /**
     * 文件扩展名
     */
    private String ext;
    /**
     * 文件的MIME类型
     */
    private String mime;
    /**
     * 文件的Sha512哈希值
     */
    private String hash;
    /**
     * 文件的上传者ID
     */
    private long uploader;
    /**
     * 文件的父文件夹ID
     */
    private long folder;
    /**
     * 文件的大小 (bytes)
     */
    private long size;
    /**
     * 文件的上传时间
     */
    @Column(onInsertValue = "now()")
    private Date uploadTime;

}