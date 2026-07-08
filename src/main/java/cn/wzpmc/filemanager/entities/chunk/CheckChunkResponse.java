package cn.wzpmc.filemanager.entities.chunk;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 在检查区块是否存在时使用的响应
 */
@Data
@AllArgsConstructor
public class CheckChunkResponse {
    /**
     * 区块哈希值
     */
    private String hash;
    /**
     * 区块ID（当区块存在时，存在的区块ID）
     */
    private Long chunkId;
}
