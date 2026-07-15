package cn.wzpmc.filemanager.entities.vo;

import com.alibaba.fastjson2.annotation.JSONCompiled;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import lombok.Data;

/**
 * 指纹对象
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
    private Long userId;
}
