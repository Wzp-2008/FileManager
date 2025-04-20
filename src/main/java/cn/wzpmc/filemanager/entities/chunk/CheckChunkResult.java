package cn.wzpmc.filemanager.entities.chunk;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CheckChunkResult {
    private String hash;
    private Long chunkId;
}
