package cn.wzpmc.filemanager.service;

import cn.wzpmc.filemanager.config.FileManagerProperties;
import cn.wzpmc.filemanager.entities.PageResult;
import cn.wzpmc.filemanager.entities.Result;
import cn.wzpmc.filemanager.entities.files.DeleteRequest;
import cn.wzpmc.filemanager.entities.files.FolderCreateRequest;
import cn.wzpmc.filemanager.entities.files.RawFileObject;
import cn.wzpmc.filemanager.entities.files.enums.FileType;
import cn.wzpmc.filemanager.entities.statistics.enums.Actions;
import cn.wzpmc.filemanager.entities.user.enums.Auth;
import cn.wzpmc.filemanager.entities.vo.FileVo;
import cn.wzpmc.filemanager.entities.vo.FolderVo;
import cn.wzpmc.filemanager.entities.vo.UserVo;
import cn.wzpmc.filemanager.interfaces.FilePathService;
import cn.wzpmc.filemanager.mapper.FileMapper;
import cn.wzpmc.filemanager.mapper.FolderMapper;
import cn.wzpmc.filemanager.utils.JwtUtils;
import cn.wzpmc.filemanager.utils.RandomUtils;
import cn.wzpmc.filemanager.utils.SizeStatisticsDigestInputStream;
import com.alibaba.fastjson2.JSONObject;
import com.mybatisflex.core.audit.http.HashUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.tika.Tika;
import org.apache.tomcat.util.http.fileupload.FileItemStream;
import org.apache.tomcat.util.http.fileupload.FileUpload;
import org.apache.tomcat.util.http.fileupload.impl.FileItemIteratorImpl;
import org.apache.tomcat.util.http.fileupload.servlet.ServletRequestContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static cn.wzpmc.filemanager.entities.vo.table.FileVoTableDef.FILE_VO;
import static cn.wzpmc.filemanager.entities.vo.table.FolderVoTableDef.FOLDER_VO;
import static com.mybatisflex.core.query.QueryMethods.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {
    private final FileMapper fileMapper;
    private final FolderMapper folderMapper;
    private final RandomUtils randomUtils;
    private final FileManagerProperties properties;
    private final StatisticsService statisticsService;
    private final RedisTemplate<String, FileVo> linkMapper;
    /*private final RedisTemplate<String, Long> linkCountMapper;*/
    private final StringRedisTemplate idAddrLinkMapper;
    private final JwtUtils jwtUtils;
    private final FilePathService pathService;
    public static final String ID_ADDR_PREFIX = "ID_ADDR_";
    public static final String SHARE_PREFIX = "SHARE_";
    public static final char PATH_SEPARATOR_CHAR = '/';
    public static final String PATH_SEPARATOR = "" + PATH_SEPARATOR_CHAR;

    protected void tryDeleteOrDeleteOnExit(File tmpFile) {
        if (!tmpFile.delete()) {
            log.error("delete tmp file error");
            tmpFile.deleteOnExit();
        }
    }

    @SneakyThrows
    @Transactional
    public Result<FileVo> simpleUpload(MultipartHttpServletRequest request, UserVo user, String address) {
        long folderParams = getFolderParams(request);
        ServletRequestContext servletRequestContext = new ServletRequestContext(request);
        FileUpload upload = new FileUpload();
        FileItemIteratorImpl fileItemIterator = new FileItemIteratorImpl(upload, servletRequestContext);
        FileVo lastUploadFile = null;
        while (fileItemIterator.hasNext()) {
            FileItemStream next = fileItemIterator.next();
            String fieldName = next.getFieldName();
            if (fieldName.equals("file")) {
                String name = next.getName();
                int i = name.lastIndexOf(".");
                String extName = null;
                String start = name;
                if (i != -1) {
                    start = name.substring(0, i);
                }
                if (!(i == -1 || i == name.length() - 1)) {
                    extName = name.substring(i + 1);
                }
                if (fileMapper.selectCountByCondition(FILE_VO.NAME.eq(start).and(FILE_VO.EXT.eq(extName)).and(FILE_VO.FOLDER.eq(folderParams))) > 0) {
                    return Result.failed(HttpStatus.CONFLICT, "存在同名文件，请改名或删除后重试！");
                }
                InputStream inputStream = next.openStream();
                SizeStatisticsDigestInputStream digestInputStream = new SizeStatisticsDigestInputStream(inputStream, DigestUtils.getSha512Digest());
                File savePath = properties.getSavePath();
                File tmpFile = new File(savePath, "cache-" + randomUtils.generatorRandomFileName(20));
                try (FileOutputStream fileOutputStream = new FileOutputStream(tmpFile)) {
                    StreamUtils.copy(digestInputStream, fileOutputStream);
                }
                digestInputStream.close();
                String hex = HashUtil.toHex(digestInputStream.getMessageDigest().digest());
                long size = digestInputStream.getSize();
                if (size == 0) {
                    tryDeleteOrDeleteOnExit(tmpFile);
                    return Result.failed(HttpStatus.LENGTH_REQUIRED, "请勿上传空文件！");
                }
                Tika tika = new Tika();
                String detect = tika.detect(tmpFile);
                FileVo fileVo = new FileVo();
                fileVo.setUploader(user.getId());
                fileVo.setMime(detect);
                fileVo.setSize(size);
                fileVo.setName(start);
                fileVo.setExt(extName);
                fileVo.setHash(hex);
                fileVo.setFolder(folderParams);
                fileMapper.insert(fileVo);
                statisticsService.insertAction(user, Actions.UPLOAD, JSONObject.of("id", fileVo.getId(), "hex", hex, "address", address));
                File targetFile = new File(savePath, hex);
                lastUploadFile = fileVo;
                if (targetFile.isFile()) {
                    tryDeleteOrDeleteOnExit(tmpFile);
                    continue;
                }
                if (!tmpFile.renameTo(targetFile)) {
                    throw new RuntimeException("error while moving file");
                }
            }
        }
        if (lastUploadFile == null) {
            return Result.failed(HttpStatus.BAD_REQUEST, "未找到文件参数");
        }
        return Result.success("上传成功！", lastUploadFile);
    }

    private static long getFolderParams(MultipartHttpServletRequest request) {
        String params = request.getQueryString();
        long folderParams = -1L;
        if (params != null && !params.isEmpty()) {
            String[] param = params.split("&");
            for (String s : param) {
                String[] keyValue = s.split("=");
                if (keyValue.length == 2) {
                    String key = keyValue[0];
                    String value = keyValue[1];
                    if (key.equals("folder")) {
                        folderParams = Long.parseLong(value);
                    }
                }
            }
        }
        return folderParams;
    }

    public Result<PageResult<RawFileObject>> getFilePager(long page, int num, long folder, String address) {
        QueryWrapper queryWrapper = select(
                FILE_VO.ID.as("id"),
                FILE_VO.NAME.as("name"),
                FILE_VO.EXT.as("ext"),
                FILE_VO.SIZE.as("size"),
                FILE_VO.FOLDER.as("parent"),
                FILE_VO.UPLOADER.as("owner"),
                FILE_VO.UPLOAD_TIME.as("time"),
                string("FILE").as("type")
        ).from(FILE_VO).
                where(FILE_VO.FOLDER.eq(folder)).
                unionAll(
                        select(
                                FOLDER_VO.ID.as("id"),
                                FOLDER_VO.NAME.as("name"),
                                null_().as("ext"),
                                number(-1).as("size"),
                                FOLDER_VO.PARENT.as("parent"),
                                FOLDER_VO.CREATOR.as("owner"),
                                FOLDER_VO.CREATE_TIME.as("time"),
                                string("FOLDER").as("type")
                        ).from(FOLDER_VO).
                                where(FOLDER_VO.PARENT.eq(folder))
                ).
                orderBy(
                        column("time").
                                asc(),
                        column("id").
                                asc()
                );
        Page<RawFileObject> paginate = fileMapper.paginateAs(page, num, queryWrapper, RawFileObject.class);
        PageResult<RawFileObject> result = new PageResult<>(paginate.getTotalRow(), paginate.getRecords());
        return Result.success(result);
    }

    public Result<FolderVo> mkdir(FolderCreateRequest request, UserVo user, String address) {
        String name = request.getName();
        long parent = request.getParent();
        if (fileMapper.selectCountByCondition(FILE_VO.EXT.eq(null_()).and(FILE_VO.NAME.eq(name)).and(FILE_VO.FOLDER.eq(parent))) > 0) {
            return Result.failed(HttpStatus.CONFLICT, "创建文件夹失败，同名文件已存在！");
        }
        if (folderMapper.selectCountByCondition(FOLDER_VO.NAME.eq(name).and(FOLDER_VO.PARENT.eq(parent))) > 0) {
            return Result.failed(HttpStatus.CONFLICT, "创建文件夹失败，同名文件已存在！");
        }
        FolderVo folderVo = new FolderVo();
        folderVo.setCreator(user.getId());
        folderVo.setName(name);
        folderVo.setParent(parent);
        folderMapper.insert(folderVo);
        statisticsService.insertAction(user, Actions.UPLOAD, JSONObject.of("type", "folder", "id", folderVo.getId(), "address", address));
        return Result.success("创建成功", folderVo);
    }

    protected void deleteFile(FileVo fileVo) {
        if (fileMapper.selectCountByCondition(FILE_VO.HASH.eq(fileVo.getHash())) <= 0) {
            String hash = fileVo.getHash();
            File file = new File(properties.getSavePath(), hash);
            if (file.exists()) {
                if (!file.delete()) {
                    log.error("删除文件 {} 失败！", file);
                }
            } else {
                log.error("存储目录可能损坏，在删除文件 {} 时未发现文件", file);
            }
        }
    }

    @Transactional
    public Result<Void> delete(DeleteRequest request, UserVo user, String address) {
        long id = request.getId();
        FileType type = request.getType();
        long actorId = user.getId();
        if (type.equals(FileType.FILE)) {
            FileVo fileVo = fileMapper.selectOneById(id);
            if (fileVo == null) {
                return Result.failed(HttpStatus.NOT_FOUND, "文件不存在！");
            }
            if (user.getAuth().equals(Auth.user)) {
                if (fileMapper.selectCountByCondition(FILE_VO.ID.eq(id).and(FILE_VO.UPLOADER.eq(actorId))) <= 0) {
                    return Result.failed(HttpStatus.UNAUTHORIZED, "权限不足！");
                }
            }
            fileMapper.deleteById(fileVo.getId());
            deleteFile(fileVo);
        } else {
            FolderVo folder = folderMapper.selectOneById(id);
            if (folder == null) {
                return Result.failed(HttpStatus.NOT_FOUND, "文件不存在！");
            }
            if (user.getAuth().equals(Auth.user)) {
                if (folderMapper.selectCountByCondition(FOLDER_VO.ID.eq(id).and(FOLDER_VO.CREATOR.eq(actorId))) <= 0) {
                    return Result.failed(HttpStatus.UNAUTHORIZED, "权限不足！");
                }
            }
            fileMapper.deleteByCondition(FILE_VO.FOLDER.eq(id));
            for (FileVo fileVo : fileMapper.selectListByCondition(FILE_VO.FOLDER.eq(id))) {
                deleteFile(fileVo);
            }
        }
        statisticsService.insertAction(user, Actions.DELETE, JSONObject.of("id", id, "type", type, "address", address));
        return Result.success();
    }

    public Result<FileVo> getFileDetail(long id) {
        FileVo fileVo = this.fileMapper.selectOneById(id);
        if (fileVo == null) {
            return Result.failed(HttpStatus.NOT_FOUND, "未知文件");
        }
        return Result.success(fileVo);
    }

    @SneakyThrows
    public void downloadFile(String id, String range, HttpServletResponse response) {
        FileVo fileVo = linkMapper.opsForValue().get(id);
        if (fileVo == null) {
            Result.failed(HttpStatus.NOT_FOUND, "未知文件！").writeToResponse(response);
            return;
        }
        long size = fileVo.getSize();
        long min = 0;
        long max = size;
        if (!range.equals("null")) {
            String[] unitRanges = range.split("=");
            String[] minMax = unitRanges[1].split("-");
            if (minMax.length > 0) {
                min = Long.parseLong(minMax[0]);
            }
            if (minMax.length > 1) {
                max = Long.parseLong(minMax[1]);
            }
        }
        String hash = fileVo.getHash();
        File file = new File(properties.getSavePath(), hash);
        String fullName = fileVo.getName();
        String ext = fileVo.getExt();
        if (ext != null) {
            fullName += '.' + ext;
        }
        response.setStatus(206);
        response.addHeader("Content-Length", String.valueOf(max - min));
        response.addHeader("Content-Range", "bytes " + min + "-" + max + "/" + size);
        response.addHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fullName, StandardCharsets.UTF_8));
        ServletOutputStream outputStream = response.getOutputStream();
        try (FileInputStream fis = new FileInputStream(file)) {
            StreamUtils.copyRange(fis, outputStream, min, max);
        }
        outputStream.flush();
    }

    public Result<String> getFileLink(long id, String address, HttpServletRequest request) {
        FileVo fileVo = fileMapper.selectOneById(id);
        long fileId = fileVo.getId();
        String identify = ID_ADDR_PREFIX + fileId + address;
        String link = idAddrLinkMapper.opsForValue().get(identify);
        if (link == null) {
            link = randomUtils.generatorRandomFileName(8);
            String authorization = request.getHeader("Authorization");
            if (authorization != null) {
                jwtUtils.getUser(authorization).ifPresent(uid -> statisticsService.insertAction(new UserVo(uid), Actions.DOWNLOAD, JSONObject.of("id", fileId, "address", address)));
            } else {
                statisticsService.insertAction(Actions.DOWNLOAD, JSONObject.of("id", fileId, "address", address));
            }
            linkMapper.opsForValue().set(link, fileVo, 30, TimeUnit.MINUTES);
            idAddrLinkMapper.opsForValue().set(identify, link, 30, TimeUnit.MINUTES);
        }
        return Result.success("成功", link);
    }

    public Result<String> shareFile(Long id, Date lastCouldDownloadTime, int maxDownloadCount) {
        FileVo fileVo = this.fileMapper.selectOneById(id);
        if (fileVo == null) {
            return Result.failed(HttpStatus.NOT_FOUND, "未知文件");
        }
        String linkName = randomUtils.generatorRandomFileName(10);
        String innerId = randomUtils.generatorRandomString(30);
        long expireAfterMs = lastCouldDownloadTime.getTime() - new Date().getTime();
        return Result.success(linkName);
    }

    public Result<RawFileObject> resolveFileDetail(String path) {
        String[] split = path.split(PATH_SEPARATOR);
        RawFileObject fileObj = pathService.resolveFile(split);
        if (fileObj == null) {
            return Result.failed(HttpStatus.NOT_FOUND, "文件不存在！");
        }
        return Result.success(fileObj);
    }

    public Result<String> findFilePathById(long id) {
        FileVo fileVo = fileMapper.selectOneById(id);
        if (fileVo == null) {
            return Result.failed(HttpStatus.NOT_FOUND, "文件不存在！");
        }
        return Result.success("成功", pathService.getFilePath(RawFileObject.of(fileVo)));
    }

    public Result<String> findFolderPathById(long id) {
        FolderVo folderVo = folderMapper.selectOneById(id);
        if (folderVo == null) {
            return Result.failed(HttpStatus.NOT_FOUND, "文件夹不存在");
        }
        return Result.success("成功", pathService.getFilePath(RawFileObject.of(folderVo)));
    }
}