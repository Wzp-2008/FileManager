package cn.wzpmc.filemanager.service;

import cn.wzpmc.filemanager.configurations.FileManagerConfiguration;
import cn.wzpmc.filemanager.entities.FileObject;
import cn.wzpmc.filemanager.entities.Page;
import cn.wzpmc.filemanager.entities.Result;
import cn.wzpmc.filemanager.entities.User;
import cn.wzpmc.filemanager.entities.vo.FileObjectVo;
import cn.wzpmc.filemanager.enums.Auth;
import cn.wzpmc.filemanager.enums.HttpCodes;
import cn.wzpmc.filemanager.enums.SearchType;
import cn.wzpmc.filemanager.mapper.FileMapper;
import cn.wzpmc.filemanager.mapper.UserMapper;
import cn.wzpmc.filemanager.utils.JwtUtils;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import com.alibaba.fastjson2.JSONObject;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class FileService {
    private final FileMapper fileMapper;
    private final UserMapper userMapper;
    private final File savePath;
    private final File tmpPath;
    private final JwtUtils jwtUtils;
    private final RedisTemplate<String, String> redisTemplate;
    private final AccessService accessService;

    @Autowired
    public FileService(FileMapper fileMapper, UserMapper userMapper, AccessService accessService, FileManagerConfiguration configuration, JwtUtils jwtUtils, RedisTemplate<String, String> redisTemplate){
        this.fileMapper = fileMapper;
        this.userMapper = userMapper;
        this.accessService = accessService;
        this.savePath = configuration.getSavePath();
        this.tmpPath = configuration.getTmpPath();
        this.jwtUtils = jwtUtils;
        this.fileMapper.createDefault();
        this.redisTemplate = redisTemplate;
    }
    private FileObject getFilenameExtend(String fullName){
        StringBuilder filename = new StringBuilder();
        String fileExtend = null;
        for (int i = 0; i < fullName.length(); i++) {
            char c = fullName.charAt(i);
            if (c == '.') {
                if (!Objects.isNull(fileExtend)) {
                    filename.append(".").append(fileExtend);
                }
                fileExtend = "";
                continue;
            }
            if (Objects.nonNull(fileExtend)){
                fileExtend += c;
            } else {
                filename.append(c);
            }
        }
        FileObject result = new FileObject();
        result.setName(filename.toString());
        result.setType(fileExtend);
        return result;
    }
    public Result<FileObjectVo> uploadFile(MultipartFile multipartFile, String authorization) {
        Optional<User> optionalUser = jwtUtils.getUser(authorization);
        if (optionalUser.isEmpty()){
            return Result.failed(HttpCodes.ACCESS_DENIED);
        }
        User user = optionalUser.get();
        int userId = user.getId();
        String originalName = multipartFile.getOriginalFilename();
        if (Objects.isNull(originalName)){
            return Result.failed(HttpCodes.HTTP_CODES401);
        }
        FileObject filenameExtend = getFilenameExtend(originalName);
        String name = filenameExtend.getName();
        String fileExtend = filenameExtend.getType();
        if (this.fileMapper.getFileCountByNameAndType(name, fileExtend) >= 1){
            // 文件存在
            return Result.failed(HttpCodes.HTTP_CODES401);
        }
        String tmpFileName = JwtUtils.generatorRandomLowerStandString(8);
        File tmpFile = new File(this.tmpPath, tmpFileName);
        try {
            if (!tmpFile.createNewFile()) {
                log.error("无法创建文件：{}", tmpFile.getAbsoluteFile());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            multipartFile.transferTo(tmpFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String sha512;
        try(FileInputStream fileInputStream = new FileInputStream(tmpFile)){
            sha512 = DigestUtils.sha512Hex(fileInputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        File file = new File(this.savePath, sha512);
        if (!file.isFile()){
            try {
                Files.move(tmpFile.toPath(), file.toPath());
            } catch (IOException e) {
                log.error("无法移动文件：{} -> {}", tmpPath.getAbsoluteFile(), file.getAbsoluteFile());
                e.printStackTrace();
            }
        }
        long length = file.length();
        FileObject fileObject = new FileObject();
        fileObject.setName(name);
        fileObject.setType(fileExtend);
        fileObject.setSize(length);
        fileObject.setHash(sha512);
        fileObject.setUploader(userId);
        fileObject.setUploadTime(new Date());
        fileMapper.addFile(fileObject);
        return Result.success(fileMapper.getFileVo(fileObject.getId()));
    }

    public Result<String> generatorDownloadLink(int id, HttpServletRequest request) {
        FileObject fileById = this.fileMapper.getFileById(id);
        if (Objects.isNull(fileById)){
            return Result.failed(HttpCodes.FILE_NOT_FOUND);
        }
        String remoteAddr = request.getRemoteHost();
        String addrIdKeys = "addr:" + remoteAddr + "id:" + id;
        ValueOperations<String, String> stringStringValueOperations = this.redisTemplate.opsForValue();
        String downloadLink;
        String usableLink = stringStringValueOperations.get(addrIdKeys);
        if (Objects.nonNull(usableLink)){
            downloadLink = usableLink;
        } else {
            downloadLink = JwtUtils.generatorRandomLowerStandString(10);
            this.redisTemplate.opsForValue().set("link:" + downloadLink, JSON.toJSONString(fileById), 15, TimeUnit.MINUTES);
            this.redisTemplate.opsForValue().set(addrIdKeys, downloadLink, 15, TimeUnit.MINUTES);
            this.fileMapper.addDownloadCount(id);
            accessService.addDownloadCounter();
        }
        return Result.success(downloadLink);
    }

    public void downloadFile(String link, HttpServletResponse response) throws IOException {
        ValueOperations<String, String> stringStringValueOperations = this.redisTemplate.opsForValue();
        FileObject fileObject;
        try {
            JSONObject jsonObject = JSON.parseObject(stringStringValueOperations.get("link:" + link));
            if (Objects.isNull(jsonObject)){
                Result.failed(HttpCodes.FILE_NOT_FOUND).writeToResponse(response);
                return;
            }
            fileObject = jsonObject.to(FileObject.class);
        }catch (JSONException exception){
            Result.failed(HttpCodes.FILE_NOT_FOUND).writeToResponse(response);
            return;
        }
        String hash = fileObject.getHash();
        File targetFile = new File(this.savePath, hash);
        response.setContentLengthLong(fileObject.getSize());
        response.setHeader("Content-Disposition", "attachment;filename=" + fileObject.generatorFileName());
        try(FileInputStream fis = new FileInputStream(targetFile); ServletOutputStream outputStream = response.getOutputStream()){
            StreamUtils.copy(fis, outputStream);
        }
    }

    public Result<Page<FileObjectVo>> getAllFile(int page, int num) {
        List<FileObjectVo> pageFile = this.fileMapper.getPageFile((page - 1) * num, num);
        int allFileCount = this.fileMapper.getAllFileCount();
        return Result.page(allFileCount, pageFile);
    }

    public Result<Boolean> removeFile(int id, String authorization) {
        Optional<User> user = this.jwtUtils.getUser(authorization);
        if (user.isEmpty()){
            return Result.failed(HttpCodes.ACCESS_DENIED);
        }
        if (Auth.user.equals(this.userMapper.getUserAuthById(user.get().getId()))){
            return Result.failed(HttpCodes.ACCESS_DENIED);
        }
        FileObject fileById = this.fileMapper.getFileById(id);
        if (Objects.isNull(fileById)){
            return Result.failed(HttpCodes.FILE_NOT_FOUND);
        }
        File saveFile = new File(this.savePath, fileById.getHash());
        if (!saveFile.exists()) {
            log.warn("文件{}不存在，进行逻辑删除", fileById);
        }else {
            if (!saveFile.delete()) {
                log.error("文件{}删除失败！", fileById);
                return Result.failed(HttpCodes.HTTP_CODES500);
            }
        }
        this.fileMapper.removeFile(id);
        return Result.success(true);
    }

    @SneakyThrows
    public Result<InputStream> getFileContentById(int id) {
        String hashById = this.fileMapper.getHashById(id);
        if (Objects.isNull(hashById)){
            Result.failed(HttpCodes.FILE_NOT_FOUND);
        }
        File targetFile = new File(this.savePath, hashById);
        if (!targetFile.isFile()) {
            return Result.failed(HttpCodes.HTTP_CODES500);
        }
        return Result.success(new FileInputStream(targetFile));
    }

    @SneakyThrows
    public Result<Page<FileObjectVo>> search(int page, int num, SearchType searchType, String data) {
        return switch (searchType) {
            case ID -> {
                long id = Long.parseLong(data);
                FileObjectVo fileById = this.fileMapper.getFileVo(id);
                List<FileObjectVo> result = new ArrayList<>();
                if(fileById == null){
                    yield Result.success(new Page<>(0, result));
                }
                result.add(fileById);
                yield Result.success(new Page<>(1, result));
            }
            case TYPE -> {
                List<FileObjectVo> result = this.fileMapper.getFileByType(data, (page - 1) * num, num);
                int total = this.fileMapper.getFileCountByType(data);
                yield Result.success(new Page<>(total, result));
            }
            case UPLOADER -> {
                String searchInput = "%" + data + "%";
                List<FileObjectVo> result = this.fileMapper.getFileByUploader(searchInput, (page - 1) * num, num);
                int total = this.fileMapper.getFileCountByUploader(searchInput);
                yield Result.success(new Page<>(total, result));
            }
            case FILE_NAME -> {
                String searchInput = "%" + data + "%";
                List<FileObjectVo> result = this.fileMapper.getFileByName(searchInput, (page - 1) * num, num);
                int total = this.fileMapper.getFileCountByName(searchInput);
                yield Result.success(new Page<>(total, result));
            }
            case UPLOAD_DAY -> {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Date parse = simpleDateFormat.parse(data);
                Calendar instance = Calendar.getInstance();
                instance.setTime(parse);
                instance.set(Calendar.HOUR_OF_DAY, 0);
                instance.set(Calendar.MINUTE, 0);
                instance.set(Calendar.SECOND, 0);
                Date date = instance.getTime();
                List<FileObjectVo> result = this.fileMapper.getFileByDate(date, (page - 1) * num, num);
                int total = this.fileMapper.getFileCountByDate(date);
                yield Result.success(new Page<>(total, result));
            }
        };
    }

    public Result<Boolean> checkUpload(String filename) {
        FileObject filenameExtend = getFilenameExtend(filename);
        String name = filenameExtend.getName();
        String type = filenameExtend.getType();
        return Result.success(this.fileMapper.getFileCountByNameAndType(name, type) == 0);
    }
}
