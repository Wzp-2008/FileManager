package cn.wzpmc.filemanager.entities.fingerprint;

import com.alibaba.fastjson2.annotation.JSONCompiled;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @since 2025/5/10 14:20
 * @author wzp
 * @version 1.0.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JSONCompiled
public class FingerprintRequest {
    /**
     * 浏览器指纹
     */
    private String fingerprint;
}
