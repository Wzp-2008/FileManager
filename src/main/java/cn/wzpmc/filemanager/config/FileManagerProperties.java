package cn.wzpmc.filemanager.config;

import lombok.Data;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.File;

@ConfigurationProperties(prefix = "wzp.filemanager")
@Data
public class FileManagerProperties {
    @Getter
    private File savePath;
    private String hmacKey = "RANDOM";
    private FFmpegConfiguration ffmpeg;
}