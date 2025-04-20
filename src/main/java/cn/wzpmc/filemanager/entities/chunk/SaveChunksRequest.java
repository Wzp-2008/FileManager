package cn.wzpmc.filemanager.entities.chunk;

import lombok.Data;

import java.util.List;

@Data
public class SaveChunksRequest {
    private String filename;
    private List<Long> chunks;
    private Long folderId;
}
