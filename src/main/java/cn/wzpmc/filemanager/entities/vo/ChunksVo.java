package cn.wzpmc.filemanager.entities.vo;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

@Table("chunks")
@Data
public class ChunksVo {
    /**
     * 区块ID
     */
    @Id(keyType = KeyType.Auto)
    private long id;
    /**
     * 区块哈希值
     */
    private String hash;
    /**
     * 区块大小
     */
    private long size;
}
