package cn.wzpmc.filemanager.entities.p2p;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.web.socket.TextMessage;

import java.util.UUID;

@Data
@AllArgsConstructor
public class ChannelEvent<T> {
    /**
     * 从谁来
     */
    private String from;
    /**
     * 指令
     */
    private String cmd;
    /**
     * 内容
     */
    private T data;

    public static TextMessage ofConsole(String cmd, Object data) {
        return new TextMessage(JSON.toJSONString(new ChannelEvent<>("CONSOLE", cmd, data)));
    }

    public static TextMessage of(UUID from, String cmd, Object data) {
        return new TextMessage(JSON.toJSONString(new ChannelEvent<>(from.toString(), cmd, data)));
    }

    public static TextMessage ofData(UUID from, JSONObject originalData) {
        return of(from, originalData.getString("cmd"), originalData.get("data"));
    }
}
