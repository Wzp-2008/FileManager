package cn.wzpmc.filemanager.entities.chunk;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CheckChunkResult {
    /**
     * 区块哈希值
     */
    private String hash;
    /**
     * 区块ID（当区块存在时，存在的区块ID）
     */
    private Long chunkId;
}
