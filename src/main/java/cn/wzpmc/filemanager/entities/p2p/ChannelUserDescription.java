package cn.wzpmc.filemanager.entities.p2p;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class ChannelUserDescription {
    private List<UUID> receiver;
    private UUID sender;

    public ChannelUserDescription() {
        this.receiver = new ArrayList<>();
    }
}
