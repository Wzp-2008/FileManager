package cn.wzpmc.filemanager.entities.files;

import com.alibaba.fastjson2.annotation.JSONCompiled;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 带所有者名称(ownerName)和下载次数(downCount)的文件对象描述
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@JSONCompiled
public class FullRawFileObject extends RawFileObject {
    /**
     * 文件所有者名称
     */
    private String ownerName;
    /**
     * 文件下载次数
     */
    private long downCount;
}
