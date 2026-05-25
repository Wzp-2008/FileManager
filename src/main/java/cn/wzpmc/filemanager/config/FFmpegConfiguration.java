package cn.wzpmc.filemanager.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.File;

/**
 * ffmpeg相关配置
 */
@ConfigurationProperties(prefix = "wzp.filemanager.ffmpeg")
@Data
public class FFmpegConfiguration {
    /**
     * ffmpeg 可执行文件位置 如：/usr/bin/ffmpeg，若不填写会在系统path中查找
     */
    private File ffmpegPath;
    /**
     * ffprobe 可执行文件位置 如：/usr/bin/ffprobe，若不填写会在系统path中查找
     */
    private File ffprobePath;
}