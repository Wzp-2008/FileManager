package cn.wzpmc.filemanager.entities.chunk;

import lombok.Data;

import java.util.List;

/**
 * 区块保存为文件的请求
 */
@Data
public class SaveChunksRequest {
    /**
     * 文件名
     */
    private String filename;
    /**
     * 区块列表
     */
    private List<Long> chunks;
    /**
     * 文件夹ID（根目录为-1）
     */
    private Long folderId;
}
