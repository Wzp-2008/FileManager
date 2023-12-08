package cn.wzpmc.filemanager.configurations;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Objects;

@Component
@Slf4j
public class FileManagerConfiguration {
    @Value("${wzp.filemanager.save-path}")
    private File savePath;
    public File getSavePath(){
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
    @Value("${wzp.filemanager.ffmpeg-path}")
    private String ffmpegPath;
    private File ffmpegExecuteableFile = null;
    public File getFFMpegExecutebleFile(){
        if (ffmpegPath == null){
            return null;
        }
        if (Objects.isNull(ffmpegExecuteableFile)){
            if ("AUTO".equalsIgnoreCase(ffmpegPath)){
                ffmpegExecuteableFile = searchFileInPath("ffmpeg");
            }else {
                ffmpegExecuteableFile = new File(ffmpegPath);
                if (!ffmpegExecuteableFile.isFile()){
                    this.ffmpegExecuteableFile = null;
                }
            }
        }
        if (ffmpegExecuteableFile == null){
            log.error("找不到FFMpeg可执行程序，请尝试指定wzp.filemanager.ffmpeg-path");
        }
        return ffmpegExecuteableFile;
    }
    @Value("${wzp.filemanager.ffprobe-path}")
    private String ffprobePath;
    private File ffprobeExecutebleFile = null;
    public File getFFProbeExecutebleFile(){
        if (ffprobePath == null){
            return null;
        }
        if (Objects.isNull(ffprobeExecutebleFile)){
            if ("AUTO".equalsIgnoreCase(ffprobePath)){
                ffprobeExecutebleFile = searchFileInPath("ffprobe");
            } else {
                ffprobeExecutebleFile = new File(ffprobePath);
                if (!ffprobeExecutebleFile.isFile()){
                    this.ffprobeExecutebleFile = null;
                }
            }
        }
        if (ffprobeExecutebleFile == null){
            log.error("找不到FFProbe可执行程序，请尝试指定wzp.filemanager.ffprobe-path");
        }
        return ffprobeExecutebleFile;
    }
    @Value("${wzp.filemanager.tmp-path}")
    private File tmpPath;
    public File getTmpPath(){
        if (tmpPath == null){
            return null;
        }
        if (!tmpPath.isDirectory()){
            if (tmpPath.mkdirs()) {
                log.error("无法创建缓存目录：{}", tmpPath);
                return null;
            }
        }
        return tmpPath;
    }
}
