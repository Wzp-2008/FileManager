package cn.wzpmc.filemanager.entities;

import cn.wzpmc.filemanager.enums.EncodingStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EncodingThreadInfo {
    private EncodingStatus status;
    private FileObject file;
    private float progress;
    private long totalFrame;
    private long nowFrame;
    private float fps;
}
