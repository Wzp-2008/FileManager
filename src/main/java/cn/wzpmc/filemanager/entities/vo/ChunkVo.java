package cn.wzpmc.filemanager.entities.vo;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

@Table("chunk")
@Data
public class ChunkVo {
    @Id(keyType = KeyType.Auto)
    private int id;
    private String sha1;
    private long size;

}