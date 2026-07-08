package cn.wzpmc.filemanager.entities.fingerprint;

import com.alibaba.fastjson2.annotation.JSONCompiled;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 指纹登录请求
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
