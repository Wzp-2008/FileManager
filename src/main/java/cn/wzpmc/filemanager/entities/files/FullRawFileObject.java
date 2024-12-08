package cn.wzpmc.filemanager.entities.files;

import com.alibaba.fastjson2.annotation.JSONCompiled;
import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Table("raw_file")
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
