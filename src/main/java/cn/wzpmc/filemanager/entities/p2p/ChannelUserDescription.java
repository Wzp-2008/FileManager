package cn.wzpmc.filemanager.entities.p2p;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * P2P信令用户描述
 */
@Data
public class ChannelUserDescription {
    /**
     * 接收者列表
     */
    private List<UUID> receiver;
    /**
     * 发送者
     */
    private UUID sender;

    public ChannelUserDescription() {
        this.receiver = new ArrayList<>();
    }
}
