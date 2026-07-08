package cn.wzpmc.filemanager.entities.files;

import cn.wzpmc.filemanager.entities.files.enums.FileType;
import com.alibaba.fastjson2.annotation.JSONCompiled;
import lombok.Data;

/**
 * 移动文件请求
 */
@Data
@JSONCompiled
public class MoveFileRequest {
    /**
     * 原始文件ID
     */
    private long originalFileId;
    /**
     * 原始文件类型
     */
    private FileType fileType;
    /**
     * 新的父文件夹ID（可为空）
     */
    private Long newParentId;
    /**
     * 新的完整的文件名（包括扩展名）（可为空）
     */
    private String newFilename;
}
