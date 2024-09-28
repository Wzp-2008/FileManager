package cn.wzpmc.filemanager.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.File;

@ConfigurationProperties(prefix = "wzp.filemanager.ffmpeg")
@Data
public class FFmpegConfiguration {
    private File ffmpegPath;
    private File ffprobePath;
}