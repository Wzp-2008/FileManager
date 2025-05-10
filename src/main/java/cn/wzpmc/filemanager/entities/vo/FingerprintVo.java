package cn.wzpmc.filemanager.entities.vo;

import com.alibaba.fastjson2.annotation.JSONCompiled;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import lombok.Data;

/**
 * @since 2025/5/10 14:17
 * @author wzp
 * @version 1.0.0
 */
@Table("fingerprint")
@JSONCompiled
@Data
public class FingerprintVo {
    @Id
    private String fingerprint;
    private long userId;
}
