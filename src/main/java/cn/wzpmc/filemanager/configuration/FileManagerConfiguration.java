package cn.wzpmc.filemanager.configuration;

import cn.wzpmc.filemanager.config.FFmpegConfiguration;
import cn.wzpmc.filemanager.config.FileManagerProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Objects;

/**
 * 系统主配置（用于生成存储文件夹以及初始化ffmpeg相关的配置）
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class FileManagerConfiguration {
    // 用户输入的配置
    private final FileManagerProperties properties;

    /**
     * 文件保存文件夹<br/>
     * 执行此方法时若文件夹不存在则会尝试创建，创建失败则返回null
     *
     * @return 文件保存的文件夹
     */
    public File getSavePath() {
        File savePath = properties.getSavePath();
        if (!savePath.isDirectory()) {
            if (!savePath.mkdirs()) {
                log.error("无法创建存储文件夹：{}", savePath);
                return null;
            }
        }
        return savePath;
    }

    /**
     * 在系统path中搜索文件
     *
     * @param filename 文件名
     * @return 具体的文件对象
     */
    private File searchFileInPath(String filename) {
        // user.dir为当前程序运行目录，PATH为系统的PATH变量，以pathSeparator分割
        for (String path : (System.getProperty("user.dir") + ":" + System.getenv("PATH")).split(File.pathSeparator)) {
            File file = new File(path);
            // 若当前文件为文件夹
            if (file.isDirectory()) {
                // 在文件夹中寻找可能的文件
                File[] possibleFiles = file.listFiles((fn) -> fn.getName().equalsIgnoreCase(filename));
                // 若一个文件都没找到
                if (possibleFiles == null) {
                    // 跳过此路径
                    continue;
                }
                // 若找到则取第一个找到的文件并返回
                if (possibleFiles.length != 0) {
                    return possibleFiles[0];
                }
            }
        }
        // 若路径都搜索完了还是没找到则返回null
        return null;
    }

    // ffmpeg可执行文件缓存
    private File ffmpegExecuteableFile = null;

    /**
     * 根据用户配置和系统path获取ffmpeg可执行文件
     *
     * @return ffmpeg可执行文件
     */
    public File getFFMpegExecutebleFile() {
        // 若缓存存在则直接返回缓存
        if (Objects.nonNull(ffmpegExecuteableFile)) {
            return ffmpegExecuteableFile;
        }
        FFmpegConfiguration ffmpeg = properties.getFfmpeg();
        File ffmpegPath = ffmpeg.getFfmpegPath();
        if (ffmpegPath == null) {
            // 在系统PATH中搜索ffmpeg
            ffmpegExecuteableFile = searchFileInPath("ffmpeg");
        } else {
            ffmpegExecuteableFile = ffmpegPath;
            if (!ffmpegExecuteableFile.isFile()) {
                this.ffmpegExecuteableFile = null;
            }
        }
        if (ffmpegExecuteableFile == null) {
            log.error("找不到FFmpeg可执行程序，请尝试指定wzp.filemanager.ffmpeg.ffmpeg-path");
        }
        return ffmpegExecuteableFile;
    }

    // ffprobe可执行文件缓存
    private File ffprobeExecutebleFile = null;

    /**
     * 获取ffprobe可执行文件路径
     *
     * @return ffprobe可执行文件路径
     * @see FileManagerConfiguration#getFFMpegExecutebleFile()
     */
    public File getFFProbeExecutebleFile() {
        if (Objects.nonNull(ffprobeExecutebleFile)) {
            return this.ffprobeExecutebleFile;
        }
        FFmpegConfiguration ffmpeg = properties.getFfmpeg();
        File ffprobePath = ffmpeg.getFfprobePath();
        if (ffprobePath == null) {
            ffprobeExecutebleFile = searchFileInPath("ffprobe");
        } else {
            ffprobeExecutebleFile = ffprobePath;
            if (!ffprobeExecutebleFile.isFile()) {
                this.ffprobeExecutebleFile = null;
            }
        }
        if (ffprobeExecutebleFile == null) {
            log.error("找不到FFProbe可执行程序，请尝试指定wzp.filemanager.ffmpeg.ffprobe-path");
        }
        return ffprobeExecutebleFile;
    }
}