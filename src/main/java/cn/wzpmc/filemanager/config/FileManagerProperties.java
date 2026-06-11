package cn.wzpmc.filemanager.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.io.File;

/**
 * 分享站系统配置
 */
@Slf4j
@ConfigurationProperties(prefix = "wzp.filemanager")
@Data
public class FileManagerProperties {
    /**
     * 文件保存位置（需要为一个文件夹，若不存在则会自动创建）<br/>
     * 必填
     */
    private File savePath;
    /**
     * token密钥系统所使用的加密密钥，若为RANDOM则会每次启动都随机生成一个新密钥<br/>
     * 默认值：{@code RANDOM}
     */
    private String hmacKey = "RANDOM";
    /**
     * ffmpeg配置
     */
    private FFmpegConfiguration ffmpeg;
    /**
     * 是否为调试模式，调试模式下会关闭日志输出功能，且JSON响应会被格式化<br/>
     * 默认值：{@code false}
     */
    private boolean dev = false;
    /**
     * 是否为只读模式，只读模式下只能下载文件，无法上传文件，也不插入日志，同时不允许注册账号<br/>
     * 默认值：{@code false}
     */
    private boolean readonly = false;

    /**
     * 链接默认过期时长（单位：分钟）<br/>
     * 默认值：{@code 30}
     */
    private int linkExpireMinutes = 30;

    /**
     * P2P通道默认过期时长（单位：分钟）<br/>
     * 默认值：{@code 30}
     */
    private int p2pTunnelExpireMinutes = 30;

    @Bean
    public File savePath() {
        if (!savePath.isDirectory()) {
            if (!savePath.mkdirs()) {
                log.error("创建存储文件夹失败！");
                throw new RuntimeException("创建存储文件夹失败！");
            }
        }
        return savePath;
    }
}