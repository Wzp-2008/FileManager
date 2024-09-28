package cn.wzpmc.filemanager.configuration;

import cn.wzpmc.filemanager.config.FFmpegConfiguration;
import cn.wzpmc.filemanager.config.FileManagerProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Objects;

@Component
@Slf4j
public class FileManagerConfiguration {
    private final FileManagerProperties properties;
    @Autowired
    public FileManagerConfiguration(FileManagerProperties properties){
        this.properties = properties;
    }
    public File getSavePath(){
        File savePath = properties.getSavePath();
        if (!savePath.isDirectory()){
            if (!savePath.mkdirs()){
                log.error("无法创建存储文件夹：{}", savePath);
                return null;
            }
        }
        return savePath;
    }
    private File searchFileInPath(String filename){
        for (String path : (System.getProperty("user.dir") + ":" + System.getenv("PATH")).split(File.pathSeparator)) {
            File file = new File(path);
            if (file.isDirectory()) {
                File[] possibleFiles = file.listFiles((fn) -> fn.getName().equalsIgnoreCase(filename));
                if (possibleFiles == null){
                    continue;
                }
                if (possibleFiles.length != 0) {
                    return possibleFiles[0];
                }
            }
        }
        return null;
    }
    private File ffmpegExecuteableFile = null;
    public File getFFMpegExecutebleFile(){
        if (Objects.nonNull(ffmpegExecuteableFile)){
            return ffmpegExecuteableFile;
        }
        FFmpegConfiguration ffmpeg = properties.getFfmpeg();
        File ffmpegPath = ffmpeg.getFfmpegPath();
        if (ffmpegPath == null){
            ffmpegExecuteableFile = searchFileInPath("ffmpeg");
        } else {
            ffmpegExecuteableFile = ffmpegPath;
            if (!ffmpegExecuteableFile.isFile()){
                this.ffmpegExecuteableFile = null;
            }
        }
        if (ffmpegExecuteableFile == null){
            log.error("找不到FFmpeg可执行程序，请尝试指定wzp.filemanager.ffmpeg.ffmpeg-path");
        }
        return ffmpegExecuteableFile;
    }
    private File ffprobeExecutebleFile = null;
    public File getFFProbeExecutebleFile(){
        if (Objects.nonNull(ffprobeExecutebleFile)) {
            return this.ffprobeExecutebleFile;
        }
        FFmpegConfiguration ffmpeg = properties.getFfmpeg();
        File ffprobePath = ffmpeg.getFfprobePath();
        if (ffprobePath == null){
            ffprobeExecutebleFile = searchFileInPath("ffprobe");
        } else {
            ffprobeExecutebleFile = ffprobePath;
            if (!ffprobeExecutebleFile.isFile()){
                this.ffprobeExecutebleFile = null;
            }
        }
        if (ffprobeExecutebleFile == null){
            log.error("找不到FFProbe可执行程序，请尝试指定wzp.filemanager.ffmpeg.ffprobe-path");
        }
        return ffprobeExecutebleFile;
    }
}