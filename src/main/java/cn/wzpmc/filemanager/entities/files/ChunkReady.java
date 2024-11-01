package cn.wzpmc.filemanager.entities.files;

import lombok.Data;

import java.io.Serializable;

@Data
public class ChunkReady implements Serializable {
    private long fileId;
    private long length;
}