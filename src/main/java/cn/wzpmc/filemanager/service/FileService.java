package cn.wzpmc.filemanager.service;

import cn.wzpmc.filemanager.dao.FileDao;
import cn.wzpmc.filemanager.entities.CountableList;
import cn.wzpmc.filemanager.entities.EncodingThreadInfo;
import cn.wzpmc.filemanager.entities.FileObject;
import cn.wzpmc.filemanager.entities.User;
import cn.wzpmc.filemanager.enums.EncodingStatus;
import cn.wzpmc.filemanager.ffmpeg.FFMpegRuntime;
import cn.wzpmc.filemanager.ffmpeg.enums.VideoEncoder;
import cn.wzpmc.filemanager.ffmpeg.threads.TranscodingFileThread;
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
import java.io.IOException;
import java.nio.file.Files;
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
        DaemonThread daemonThread = new DaemonThread();
        daemonThread.start();
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
        String fullName = file.getOriginalFilename();
        assert fullName != null;
        File tempFile = File.createTempFile(fullName, "");
        FileOutputStream fileOutputStream = new FileOutputStream(tempFile);
        StreamUtils.copy(file.getInputStream(), fileOutputStream);
        fileOutputStream.close();
        return uploadFile(token, tempFile, fullName);
    }
    public FileObject uploadFile(String token, File file, String fullName){
        Optional<User> user = jwtUtils.getUser(token);
        if (user.isEmpty()) {
            return null;
        }
        User requestUser = user.get();

        assert fullName != null;
        int i = fullName.lastIndexOf('.');
        String name = fullName.substring(0, i);
        String format = fullName.substring(i + 1);
        String md5 = getFileMd5(file);
        FileObject fileObject = new FileObject();
        fileObject.setFileName(name);
        fileObject.setFileFormat(format);
        fileObject.setFileSize(file.length());
        fileObject.setUploader(requestUser.getUsername());
        fileObject.setMd5(md5);
        File saveFile = new File(this.savePath, md5);
        try {
            Files.move(file.toPath(), saveFile.toPath());
        } catch (IOException e) {
            e.printStackTrace();
            log.warn("移动文件失败！, File={}", fileObject);
            return null;
        }
        dao.uploadFile(fileObject);
        return fileObject;
    }
    @SneakyThrows
    private static String getFileMd5(File file){
        MessageDigest MD5 = MessageDigest.getInstance("MD5");
        FileInputStream fileInputStream = new FileInputStream(file);
        MD5.digest(fileInputStream.readAllBytes());
        fileInputStream.close();
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
    private final Map<Integer, TranscodingFileThread> transcodingFileThreadMap = new Hashtable<>();
    private final Map<String, EncodingStatus> statusMap = new Hashtable<>();
    @SneakyThrows
    public Object encoding(FileObject fileObject, VideoEncoder outputEnc, String token){
        if (!runtime.check()) {
            log.warn("服务端缺少FFMpeg环境（FFMpeg和FFProbe），会影响某些功能使用！");
        }
        if (transcodingFileThreadMap.containsKey(fileObject.getId())){
            return "文件存在，无法转码！";
        }
        File outputTempFile = File.createTempFile("video-encoding-" + fileObject.getMd5(), ".mp4");
        File inputFile = new File(savePath, fileObject.getMd5());
        TranscodingFileThread transcodingFileThread = new TranscodingFileThread(inputFile, outputEnc, outputTempFile, (thread) -> {
            statusMap.put(thread.getName(), EncodingStatus.END);
            this.uploadFile(token, thread.getOutput(), fileObject.getFileName() + "-" + thread.getOutputEncoder().name + ".mp4");
        }, runtime);
        statusMap.put(transcodingFileThread.getName(), EncodingStatus.WAITING);
        transcodingFileThreadMap.put(fileObject.getId(), transcodingFileThread);
        return transcodingFileThreadMap.hashCode();
    }

    public List<EncodingThreadInfo> getEncodingInfo() {
        List<EncodingThreadInfo> result = new ArrayList<>();
        for (Map.Entry<Integer, TranscodingFileThread> entry : transcodingFileThreadMap.entrySet()) {
            Integer fileId = entry.getKey();
            TranscodingFileThread value = entry.getValue();
            FileObject fullFileInfo = dao.getFullFileInfo(fileId);
            result.add(new EncodingThreadInfo(statusMap.get(value.getName()), fullFileInfo, value.getProgress(), value.getTotalFrames(), value.getFrames(), value.getFps()));
        }
        return result;
    }
    private class DaemonThread extends Thread {
        @SneakyThrows
        @Override
        public void run() {
            while (true){
                for (TranscodingFileThread value : FileService.this.transcodingFileThreadMap.values()) {
                    String name = value.getName();
                    EncodingStatus status = FileService.this.statusMap.get(name);
                    if (status.equals(EncodingStatus.RUNNING)){
                        break;
                    }
                    if (status.equals(EncodingStatus.END)){
                        continue;
                    }
                    value.start();
                    FileService.this.statusMap.put(name, EncodingStatus.RUNNING);
                    break;
                }
                Thread.sleep(1000);
            }

        }
    }
}
