package cn.wzpmc.filemanager.entities.vo;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 区块与文件对应对象
 */
@Table("chunk_file")
@Data
@AllArgsConstructor
public class ChunkFileVo {
    /**
     * 区块ID
     */
    @Column("chunk_id")
    private long chunkId;
    /**
     * 文件ID
     */
    @Column("file_id")
    private long fileId;
    /**
     * 区块顺序下标（从0开始）
     */
    private long index;
}
