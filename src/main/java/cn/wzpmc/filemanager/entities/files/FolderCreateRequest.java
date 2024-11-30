package cn.wzpmc.filemanager.entities.files;

import com.alibaba.fastjson2.annotation.JSONCompiled;
import lombok.Data;

@Data
@JSONCompiled
public class FolderCreateRequest {
    private long parent;
    private String name;
}
