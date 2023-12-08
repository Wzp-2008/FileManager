package cn.wzpmc.filemanager.entities.vo;

import lombok.Data;

import java.net.InetAddress;
import java.net.SocketAddress;

@Data
public class DownloadEntryVo {
    private String remoteAddr;
    private int fileId;
}
