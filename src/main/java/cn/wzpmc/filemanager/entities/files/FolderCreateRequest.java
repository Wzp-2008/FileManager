package cn.wzpmc.filemanager.entities.files;

import com.alibaba.fastjson2.annotation.JSONCompiled;
import lombok.Data;

@Data
@JSONCompiled
public class FolderCreateRequest {
    /**
     * 父文件夹ID
     */
    private long parent;
    /**
     * 文件名
     */
    private String name;
    /**
     * 当文件夹存在时返回已存在的文件夹
     */
    private boolean existsReturn = false;
}
