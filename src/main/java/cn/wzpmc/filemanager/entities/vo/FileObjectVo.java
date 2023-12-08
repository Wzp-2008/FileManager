package cn.wzpmc.filemanager.entities.vo;

import lombok.Data;

import java.util.Date;

@Data
public class FileObjectVo {
    private int id;
    private String name;
    private String type;
    private long size;
    private String hash;
    private String uploaderName;
    private Date uploadTime;
    private int downloadCount;
}
