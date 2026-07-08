package cn.wzpmc.filemanager.entities.p2p;

import lombok.Data;

/**
 * P2P信令通道创建请求
 */
@Data
public class ChannelCreateRequest {
    /**
     * 传输的文件名
     */
    private String filename;
    /**
     * 传输的文件大小
     */
    private long size;
}
