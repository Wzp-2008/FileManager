package cn.wzpmc.filemanager.entities.vo;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

/**
 * 区块对象
 */
@Table("chunks")
@Data
public class ChunksVo {
    /**
     * 区块ID
     */
    @Id(keyType = KeyType.Auto)
    private Long id;
    /**
     * 区块哈希值
     */
    private String hash;
    /**
     * 区块大小
     */
    private Long size;
}
