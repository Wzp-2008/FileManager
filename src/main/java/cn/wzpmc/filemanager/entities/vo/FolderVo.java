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
    /**
     * 文件夹ID
     */
    @Id(keyType = KeyType.Auto)
    private long id;
    /**
     * 文件夹名
     */
    private String name;
    /**
     * 文件夹的父文件夹ID
     */
    private long parent;
    /**
     * 文件夹的创建者
     */
    private long creator;
    /**
     * 文件夹的创建时间
     */
    @Column(onInsertValue = "now()")
    private Date createTime;

}