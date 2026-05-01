package cn.wzpmc.filemanager.service;

import cn.wzpmc.filemanager.entities.Result;
import cn.wzpmc.filemanager.entities.p2p.*;
import cn.wzpmc.filemanager.entities.vo.UserVo;
import cn.wzpmc.filemanager.utils.RandomUtils;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class P2PService {
    private final RedisTemplate<String, RawChannelDescription> channelMapper;
    private final Map<String, ChannelUserDescription> channels = new HashMap<>();
    private final RandomUtils randomUtils;

    public Result<ChannelCreateResponse> createChannel(UserVo user, ChannelCreateRequest channelCreateRequest) {
        String key = randomUtils.generatorRandomString(48);
        RawChannelDescription description = new RawChannelDescription(channelCreateRequest.getFilename(), channelCreateRequest.getSize(), user.getId(), user.getName(), key);
        String id = randomUtils.generatorRandomFileName(8);
        channelMapper.opsForValue().set(id, description);
        channels.put(id, new ChannelUserDescription());
        return Result.success(new ChannelCreateResponse(id, key));
    }

    @Repository
    @RequiredArgsConstructor
    public class ChannelWebSocketHandler extends TextWebSocketHandler {
        private final Map<UUID, WebSocketSession> sessions = new ConcurrentHashMap<>();

        @Override
        public void afterConnectionEstablished(WebSocketSession session) throws Exception {
            String id = UriComponentsBuilder.fromUri(Objects.requireNonNull(session.getUri())).build().getQueryParams().getFirst("id");
            RawChannelDescription description = channelMapper.opsForValue().get(id);
            if (description == null) {
                session.sendMessage(Result.failed("未知通道ID").wsMessage());
                session.close(CloseStatus.NOT_ACCEPTABLE);
                return;
            }
            assert id != null;
            Map<String, Object> attr = session.getAttributes();
            attr.put("channelId", id);
            attr.put("channelDesc", description);
            UUID uuid = UUID.randomUUID();
            attr.put("uid", uuid);
            sessions.put(uuid, session);
            ChannelUserDescription users = channels.get(id);
            UUID sender = users.getSender();
            if (sender != null) {
                users.getReceiver().add(uuid);
                sendTo(sender, ChannelEvent.ofConsole("user_add", uuid));
                session.sendMessage(ChannelEvent.ofConsole("init", JSONObject.of("sender", sender, "filename", description.getFilename(), "size", description.getSize())));
            }
        }

        @Override
        public void afterConnectionClosed(WebSocketSession session, @NonNull CloseStatus status) throws Exception {
            Map<String, Object> attributes = session.getAttributes();
            String channelId = (String) attributes.get("channelId");
            UUID uid = (UUID) attributes.get("uid");
            if (channelId == null || uid == null) return;
            sessions.remove(uid);
            ChannelUserDescription channelUserDescription = channels.remove(channelId);
            if (channelUserDescription == null) return;
            if (channelUserDescription.getSender().equals(uid)) {
                for (UUID uuid : channelUserDescription.getReceiver()) {
                    sendTo(uuid, Result.failed("主机断开连接").wsMessage());
                    close(uuid);
                }
            }
        }

        @Override
        protected void handleTextMessage(@NonNull WebSocketSession session, TextMessage message) throws Exception {
            Map<String, Object> attr = session.getAttributes();
            UUID uid = (UUID) attr.get("uid");
            String payload = message.getPayload();
            JSONObject jsonObject = JSON.parseObject(payload);
            String type = jsonObject.getString("type");
            if (Objects.equals(type, "auth")) {
                handleLogin(session, jsonObject.getString("key"));
                return;
            }
            if (Objects.equals(type, "to")) {
                UUID uuid = UUID.fromString(jsonObject.getString("to"));
                JSONObject data = jsonObject.getJSONObject("data");
                sendTo(uuid, ChannelEvent.ofData(uid, data));
            }
        }

        private void handleLogin(WebSocketSession session, String key) throws IOException {
            Map<String, Object> attr = session.getAttributes();
            String channelId = (String) attr.get("channelId");
            ChannelUserDescription users = channels.get(channelId);
            if (users.getSender() != null) {
                session.sendMessage(Result.failed(HttpStatus.UNAUTHORIZED, "当前项目已有上传者").wsMessage());
                return;
            }
            RawChannelDescription desc = (RawChannelDescription) attr.get("channelDesc");
            if (!desc.getKey().equals(key)) {
                session.sendMessage(Result.failed(HttpStatus.UNAUTHORIZED, "密钥不匹配").wsMessage());
                return;
            }
            UUID uid = (UUID) attr.get("uid");
            users.setSender(uid);
            attr.put("sender", true);
            session.sendMessage(Result.success("鉴权成功").wsMessage());
        }

        protected void sendTo(UUID target, TextMessage data) throws IOException {
            WebSocketSession webSocketSession = sessions.get(target);
            if (webSocketSession != null) {
                webSocketSession.sendMessage(data);
            }
        }

        protected void close(UUID target) throws IOException {
            WebSocketSession webSocketSession = sessions.get(target);
            if (webSocketSession != null) {
                webSocketSession.close();
            }
        }
    }
}
