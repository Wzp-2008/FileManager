package cn.wzpmc.filemanager.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.File;

@ConfigurationProperties(prefix = "wzp.filemanager")
@Data
public class FileManagerProperties {
    private File savePath;
    private String hmacKey = "RANDOM";
    private FFmpegConfiguration ffmpeg;
    public File getSavePath() {
        System.out.println(this.savePath);
        return this.savePath;
    }
}