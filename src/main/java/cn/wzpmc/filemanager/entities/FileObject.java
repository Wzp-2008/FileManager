package cn.wzpmc.filemanager.entities;

import lombok.Data;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Objects;

@Data
public class FileObject {
    private int id;
    private String name;
    private String type;
    private long size;
    private String hash;
    private int uploader;
    private Date uploadTime;
    private int downloadCount;

    public String generatorFileName() {
        StringBuilder stringBuilder = new StringBuilder(name);
        if (Objects.nonNull(type)){
            stringBuilder.append('.').append(type);
        }
        return URLEncoder.encode(stringBuilder.toString(), StandardCharsets.UTF_8);
    }
}
