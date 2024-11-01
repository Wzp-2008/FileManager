package cn.wzpmc.filemanager.entities.files;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class ChunkChecked implements Serializable {
    private String fileId;
    private String hash;
    private long index;
}