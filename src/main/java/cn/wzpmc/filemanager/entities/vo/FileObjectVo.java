package cn.wzpmc.filemanager.entities.vo;

import com.alibaba.fastjson2.annotation.JSONCompiled;
import lombok.Data;

import java.util.Date;

@Data
@JSONCompiled
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