package cn.wzpmc.filemanager.entities.p2p;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChannelCreateResponse {
    private String channelId;
    private String senderKey;
}
