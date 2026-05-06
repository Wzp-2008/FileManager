package cn.wzpmc.filemanager.service;

import cn.wzpmc.filemanager.entities.Result;
import cn.wzpmc.filemanager.entities.p2p.*;
import cn.wzpmc.filemanager.entities.vo.UserVo;
import cn.wzpmc.filemanager.utils.RandomUtils;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.jspecify.annotations.NonNull;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

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
        private final Map<UUID, ScheduledFuture<?>> pingScheduler = new ConcurrentHashMap<>();
        private final TaskScheduler taskScheduler;

        @Override
        public void afterConnectionEstablished(WebSocketSession session) throws Exception {
            String id = UriComponentsBuilder.fromUri(Objects.requireNonNull(session.getUri())).build().getQueryParams().getFirst("id");
            RawChannelDescription description = channelMapper.opsForValue().get(id);
            if (description == null) {
                session.sendMessage(Result.failed("未知通道ID").wsMessage());
                session.close(CloseStatus.NOT_ACCEPTABLE);
                return;
            }
            sendPing(session);
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

        @SneakyThrows
        public void sendPing(WebSocketSession session) {
            if (!session.isOpen()) {
                return;
            }
            ScheduledFuture<?> scheduledFuture = taskScheduler.schedule(() -> {
                try {
                    session.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }, Instant.now().plusSeconds(30));
            UUID uuid = UUID.randomUUID();
            session.getAttributes().put("ping", uuid);
            pingScheduler.put(uuid, scheduledFuture);
            session.sendMessage(new PingMessage(Unpooled.buffer().writeLong(uuid.getMostSignificantBits()).writeLong(uuid.getLeastSignificantBits()).nioBuffer()));
        }

        @Override
        public void afterConnectionClosed(WebSocketSession session, @NonNull CloseStatus status) throws Exception {
            Map<String, Object> attributes = session.getAttributes();
            UUID pingUUID = (UUID) attributes.get("ping");
            if (pingUUID != null) {
                ScheduledFuture<?> task = pingScheduler.remove(pingUUID);
                if (task != null) {
                    task.cancel(true);
                }
            }
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

        @Override
        protected void handlePongMessage(@NonNull WebSocketSession session, PongMessage message) {
            ByteBuf byteBuf = Unpooled.copiedBuffer(message.getPayload());
            UUID uuid = new UUID(byteBuf.readLong(), byteBuf.readLong());
            ScheduledFuture<?> remove = pingScheduler.remove(uuid);
            remove.cancel(true);
            taskScheduler.schedule(() -> sendPing(session), Instant.now().plusSeconds(10));
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
