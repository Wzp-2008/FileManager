package cn.wzpmc.filemanager.entities.vo;

import com.alibaba.fastjson2.annotation.JSONCompiled;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import lombok.Data;

/**
 * @author wzp
 * @version 1.0.0
 * @since 2025/5/10 14:17
 */
@Table("fingerprint")
@JSONCompiled
@Data
public class FingerprintVo {
    /**
     * 浏览器指纹
     */
    @Id
    private String fingerprint;
    /**
     * 用户ID
     */
    private long userId;
}
