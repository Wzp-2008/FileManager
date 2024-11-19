package cn.wzpmc.filemanager.entities.files;

import cn.wzpmc.filemanager.entities.files.enums.FileType;
import lombok.Data;

@Data
public class DeleteRequest {
    private FileType type;
    private long id;
}
