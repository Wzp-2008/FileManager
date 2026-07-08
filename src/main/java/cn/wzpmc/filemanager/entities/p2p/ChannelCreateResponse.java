package cn.wzpmc.filemanager.entities.p2p;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * P2P信令通道创建响应
 */
@Data
@AllArgsConstructor
public class ChannelCreateResponse {
    /**
     * 通道ID
     */
    private String channelId;
    /**
     * 上传者密钥
     */
    private String senderKey;
}
