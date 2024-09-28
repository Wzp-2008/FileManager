package cn.wzpmc.filemanager.entities.files;

import cn.wzpmc.filemanager.entities.vo.ChunkVo;
import lombok.Data;

import java.util.List;

@Data
public class FileData {
    private List<ChunkVo> chunks;
}