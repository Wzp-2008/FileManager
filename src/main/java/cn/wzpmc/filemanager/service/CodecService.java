package cn.wzpmc.filemanager.service;

import cn.wzpmc.filemanager.configurations.FileManagerConfiguration;
import cn.wzpmc.filemanager.entities.Result;
import cn.wzpmc.filemanager.enums.HttpCodes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.Objects;

@Service
public class CodecService {
    private final File ffmpegExecutableFile;
    private final File ffprobeExecutableFile;
    private final FileService service;
    @Autowired
    public CodecService(FileManagerConfiguration configuration, FileService service){
        this.ffmpegExecutableFile = configuration.getFFMpegExecutebleFile();
        this.ffprobeExecutableFile = configuration.getFFProbeExecutebleFile();
        this.service = service;
    }
    public Result<String> getFileType(int id) {
        Result<InputStream> fileInputStream = this.service.getFileContentById(id);
        if (fileInputStream.getStatus() != 200){
            return Result.copyFailed(fileInputStream);
        }
        try(InputStream data = fileInputStream.getData()){
            String s = URLConnection.guessContentTypeFromStream(data);
            System.out.println(s);
        } catch (IOException e) {
            return Result.failed(HttpCodes.HTTP_CODES500);
        }
        return Result.failed(HttpCodes.HTTP_CODES500);
    }
}
