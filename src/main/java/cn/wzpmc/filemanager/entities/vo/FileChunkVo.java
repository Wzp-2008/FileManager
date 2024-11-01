package cn.wzpmc.filemanager.entities.vo;

import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Data;

@Table("file_chunks")
@Data
@AllArgsConstructor
public class FileChunkVo {
    private long file;
    private long chunk;
    private long index;
}