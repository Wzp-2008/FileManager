package cn.wzpmc.filemanager.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.io.File;

@Slf4j
@ConfigurationProperties(prefix = "wzp.filemanager")
@Data
public class FileManagerProperties {
    private File savePath;
    private String hmacKey = "RANDOM";
    private FFmpegConfiguration ffmpeg;
    private boolean dev = false;

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