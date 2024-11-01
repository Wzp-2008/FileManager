package cn.wzpmc.filemanager.entities.files;

import lombok.Data;

@Data
public class PrepareUploadRequest {
    private String name;
    private String ext;
    private Long size;
    private int folder;
    private String fullSha1;
}