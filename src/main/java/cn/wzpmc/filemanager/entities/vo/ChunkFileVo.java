package cn.wzpmc.filemanager.entities.vo;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Data;

@Table("chunk_file")
@Data
@AllArgsConstructor
public class ChunkFileVo {
    @Column("chunk_id")
    private long chunkId;
    @Column("file_id")
    private long fileId;
    private long index;
}
