package cn.wzpmc.filemanager.entities.files;

import lombok.Data;

@Data
public class FolderCreateRequest {
    private long parent;
    private String name;
}
