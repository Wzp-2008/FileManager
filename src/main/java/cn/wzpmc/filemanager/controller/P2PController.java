package cn.wzpmc.filemanager.controller;

import cn.wzpmc.filemanager.annotation.AuthorizationRequired;
import cn.wzpmc.filemanager.entities.Result;
import cn.wzpmc.filemanager.entities.p2p.ChannelCreateRequest;
import cn.wzpmc.filemanager.entities.p2p.ChannelCreateResponse;
import cn.wzpmc.filemanager.entities.vo.UserVo;
import cn.wzpmc.filemanager.service.P2PService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * P2P文件操作相关接口
 */
@RequestMapping("/api/channel")
@RestController
@RequiredArgsConstructor
public class P2PController {
    private final P2PService p2pService;

    /**
     * 创建P2P通道
     *
     * @param channelCreateRequest 通道信息
     * @return 通道ID
     */
    @PostMapping("/create")
    public Result<ChannelCreateResponse> createChannel(@AuthorizationRequired UserVo user, @RequestBody ChannelCreateRequest channelCreateRequest) {
        return p2pService.createChannel(user, channelCreateRequest);
    }
}
