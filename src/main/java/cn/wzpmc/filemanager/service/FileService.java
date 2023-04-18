package cn.wzpmc.filemanager.service;

import cn.wzpmc.filemanager.dao.FileDao;
import cn.wzpmc.filemanager.entities.CountableList;
import cn.wzpmc.filemanager.entities.FileObject;
import cn.wzpmc.filemanager.entities.User;
import cn.wzpmc.filemanager.ffmpeg.FFMpegRuntime;
import cn.wzpmc.filemanager.utils.JwtUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.binary.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@Log4j2
public class FileService {
    private final FileDao dao;
    private final File savePath;
    private final JwtUtils jwtUtils;
    private final FFMpegRuntime runtime;
    @Autowired
    public FileService(FileDao dao, @Value("${save-path}") String savePath, JwtUtils jwtUtils){
        this.dao = dao;
        this.jwtUtils = jwtUtils;
        this.savePath = new File(savePath);
        if (!this.savePath.exists()){
            boolean mkdir = this.savePath.mkdirs();
            if (!mkdir){
                log.error("无法创建保存文件夹，请重试！");
            }
            log.info("成功创建保存文件夹！");
        }
        this.runtime = new FFMpegRuntime();
    }

    public Long getFileCount() {
        return dao.getFileCount();
    }

    public List<FileObject> getFiles(int page) {
        if (page <= 0){
            return new ArrayList<>();
        }
        return dao.getFiles((page - 1) * 20);
    }

    public CountableList<FileObject> searchFilesById(Integer keywords, int page) {
        return new CountableList<>(1,dao.searchFilesById(keywords, (page - 1) * 20));
    }

    public CountableList<FileObject> searchFilesByName(String keywords, int page) {
        return new CountableList<>(dao.countSearchFilesByName('%' + keywords + '%'), dao.searchFilesByName('%' + keywords + '%', (page - 1) * 20));
    }

    public CountableList<FileObject> searchFilesByMd5(String keywords, int page) {
        return new CountableList<>(dao.countSearchFilesByMd5('%' + keywords + '%'), dao.searchFilesByMd5('%' + keywords + '%', (page - 1) * 20));
    }
    public CountableList<FileObject> searchFilesByFormat(String keywords, int page) {
        return new CountableList<>(dao.countSearchFilesByFormat('%' + keywords + '%'), dao.searchFilesByFormat('%' + keywords + '%', (page - 1) * 20));
    }

    @SneakyThrows
    public void downloadFile(int id, HttpServletResponse response) {
        FileObject info = dao.getFileInfo(id);
        if (info == null){
            response.sendError(404,"不存在的文件！");
            return;
        }
        response.setContentLengthLong((long) info.getFileSize());
        response.setHeader("Content-Disposition", "attachment;filename=" + info.getFileName() + '.' + info.getFileFormat());
        File downloadFile = new File(this.savePath, info.getMd5());
        try (FileInputStream inputStream = new FileInputStream(downloadFile)) {
            StreamUtils.copy(inputStream, response.getOutputStream());
        }
    }
    @SneakyThrows
    public FileObject uploadFile(String token, MultipartFile file) {
        Optional<User> user = jwtUtils.getUser(token);
        if (user.isEmpty()) {
            return null;
        }
        User requestUser = user.get();
        String fullName = file.getOriginalFilename();
        assert fullName != null;
        int i = fullName.lastIndexOf('.');
        String name = fullName.substring(0, i);
        String format = fullName.substring(i + 1);
        String md5 = getFileMd5(file);
        FileObject fileObject = new FileObject();
        fileObject.setFileName(name);
        fileObject.setFileFormat(format);
        fileObject.setFileSize(file.getSize());
        fileObject.setUploader(requestUser.getUsername());
        fileObject.setMd5(md5);
        File saveFile = new File(this.savePath, md5);
        if (!saveFile.exists()) {
            StreamUtils.copy(file.getInputStream(), new FileOutputStream(saveFile));
        }
        dao.uploadFile(fileObject);
        return fileObject;
    }
    @SneakyThrows
    private static String getFileMd5(MultipartFile file){
        MessageDigest MD5 = MessageDigest.getInstance("MD5");
        MD5.digest(file.getBytes());
        return new String(Hex.encodeHex(MD5.digest()));
    }

    public boolean removeFile(String token, FileObject file) {
        Optional<User> user = jwtUtils.getUser(token);
        if (user.isEmpty()) {
            return false;
        }
        User requestUser = user.get();
        if (!requestUser.getUsername().equals("wzp")) {
            return false;
        }
        FileObject fileInfo = dao.getFileInfo(file.getId());
        String md5 = fileInfo.getMd5();
        File saveFile = new File(this.savePath, md5);
        if (!saveFile.exists()) {
            return false;
        }
        boolean delete = saveFile.delete();
        if (!delete){
            return false;
        }
        dao.deleteFile(file.getId());
        return true;
    }
    private static final DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public Map<String, Object> getFileDetails(int id){
        FileObject fileInfo = dao.getFullFileInfo(id);
        String md5 = fileInfo.getMd5();
        File saveFile = new File(this.savePath, md5);
        if (!saveFile.exists()) {
            return null;
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("文件ID", String.valueOf(fileInfo.getId()));
        result.put("文件名", fileInfo.getFileName());
        result.put("文件类型", fileInfo.getFileFormat());
        result.put("文件MD5", md5);
        result.put("文件上传者", fileInfo.getUploader());
        result.put("文件上传时间", formatter.format(fileInfo.getUploadTime()));
        if (runtime.check()) {
            Map<String, Object> videoInfo = runtime.getVideoInfo(saveFile);
            if (videoInfo != null) {
                result.putAll(videoInfo);
            }
        }else{
            log.warn("服务端缺少FFMpeg环境（FFMpeg和FFProbe），会影响某些功能使用！");
        }
        return result;
    }
}
