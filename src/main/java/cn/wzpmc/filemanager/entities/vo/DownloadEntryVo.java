package cn.wzpmc.filemanager.entities.vo;

import com.alibaba.fastjson2.annotation.JSONCompiled;
import lombok.Data;

import java.net.InetAddress;
import java.net.SocketAddress;

@Data
@JSONCompiled
public class DownloadEntryVo {
    private String remoteAddr;
    private int fileId;
}