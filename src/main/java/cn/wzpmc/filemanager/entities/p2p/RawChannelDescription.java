package cn.wzpmc.filemanager.entities.p2p;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@AllArgsConstructor
@Data
public class RawChannelDescription implements Serializable {
    /**
     * 传输的文件名
     */
    private String filename;
    /**
     * 传输的文件大小
     */
    private long size;
    /**
     * 对方用户ID
     */
    private long userId;
    /**
     * 对方用户名
     */
    private String username;
    /**
     * 发送者密钥
     */
    private String key;
}
