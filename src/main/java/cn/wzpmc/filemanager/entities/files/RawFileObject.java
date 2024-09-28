package cn.wzpmc.filemanager.entities.files;

import cn.wzpmc.filemanager.entities.files.enums.FileType;
import lombok.Data;

import java.util.Date;
@Data
public abstract class RawFileObject {
    private String name;
    private Integer id;
    private String uploader;
    private Date createTime;
    private FileType type;
}