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
}
