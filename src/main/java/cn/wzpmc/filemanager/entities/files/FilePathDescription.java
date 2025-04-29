package cn.wzpmc.filemanager.entities.files;

import com.alibaba.fastjson2.annotation.JSONCompiled;
import lombok.Data;


/**
 * 文件路径描述
 */
@Data
@JSONCompiled
public class FilePathDescription {
    /**
     * 文件名
     */
    private String name;
    /**
     * 目标文件夹ID
     */
    private Long folderId;
}
