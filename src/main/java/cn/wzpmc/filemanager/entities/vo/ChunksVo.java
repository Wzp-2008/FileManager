package cn.wzpmc.filemanager.entities.vo;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

@Table("chunks")
@Data
public class ChunksVo {
    @Id(keyType = KeyType.Auto)
    private long id;
    private String hash;
    private long size;
}
