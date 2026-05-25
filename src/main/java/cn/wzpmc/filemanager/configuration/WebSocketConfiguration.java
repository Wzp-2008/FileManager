package cn.wzpmc.filemanager.configuration;

import cn.wzpmc.filemanager.service.P2PService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * P2P 信令WebSocket配置
 */
@Configuration
@RequiredArgsConstructor
@EnableWebSocket
public class WebSocketConfiguration implements WebSocketConfigurer {
    private final P2PService.ChannelWebSocketHandler handler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 设置允许/api/channel（P2P通道WebSocket URL）允许所有人访问
        registry.addHandler(handler, "/api/channel").setAllowedOrigins("*");
    }
}
