package cn.wzpmc.filemanager.service;

import cn.wzpmc.filemanager.config.FileManagerProperties;
import cn.wzpmc.filemanager.entities.PageResult;
import cn.wzpmc.filemanager.entities.Result;
import cn.wzpmc.filemanager.entities.chunk.CheckChunkResult;
import cn.wzpmc.filemanager.entities.chunk.SaveChunksRequest;
import cn.wzpmc.filemanager.entities.files.FolderCreateRequest;
import cn.wzpmc.filemanager.entities.files.FullRawFileObject;
import cn.wzpmc.filemanager.entities.files.MoveFileRequest;
import cn.wzpmc.filemanager.entities.files.RawFileObject;
import cn.wzpmc.filemanager.entities.files.enums.FileType;
import cn.wzpmc.filemanager.entities.files.enums.SortField;
import cn.wzpmc.filemanager.entities.statistics.enums.Actions;
import cn.wzpmc.filemanager.entities.user.enums.Auth;
import cn.wzpmc.filemanager.entities.vo.*;
import cn.wzpmc.filemanager.interfaces.FilePathService;
import cn.wzpmc.filemanager.mapper.ChunkFileMapper;
import cn.wzpmc.filemanager.mapper.ChunksMapper;
import cn.wzpmc.filemanager.mapper.FileMapper;
import cn.wzpmc.filemanager.mapper.FolderMapper;
import cn.wzpmc.filemanager.utils.JwtUtils;
import cn.wzpmc.filemanager.utils.RandomUtils;
import cn.wzpmc.filemanager.utils.stream.SerialFileInputStream;
import cn.wzpmc.filemanager.utils.stream.SizeStatisticsDigestInputStream;
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
import org.apache.tomcat.util.buf.HexUtils;
import org.apache.tomcat.util.http.fileupload.FileItemStream;
import org.apache.tomcat.util.http.fileupload.FileUpload;
import org.apache.tomcat.util.http.fileupload.impl.FileItemIteratorImpl;
import org.apache.tomcat.util.http.fileupload.servlet.ServletRequestContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static cn.wzpmc.filemanager.entities.vo.table.ChunkFileVoTableDef.CHUNK_FILE_VO;
import static cn.wzpmc.filemanager.entities.vo.table.ChunksVoTableDef.CHUNKS_VO;
import static cn.wzpmc.filemanager.entities.vo.table.FileVoTableDef.FILE_VO;
import static cn.wzpmc.filemanager.entities.vo.table.FolderVoTableDef.FOLDER_VO;
import static cn.wzpmc.filemanager.entities.vo.table.StatisticsVoTableDef.STATISTICS_VO;
import static cn.wzpmc.filemanager.entities.vo.table.UserVoTableDef.USER_VO;
import static cn.wzpmc.filemanager.entities.vo.table.custom.StatisticsVoTableDef.STATISTICS_VO_EXT;
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
    private final ChunkFileMapper chunkFileMapper;
    private final ChunksMapper chunksMapper;
    /*private final RedisTemplate<String, Long> linkCountMapper;*/
    private final StringRedisTemplate idAddrLinkMapper;
    private final JwtUtils jwtUtils;
    private FilePathService pathService;
    private final File savePath;
    public static final String ID_ADDR_PREFIX = "ID_ADDR_";
    public static final String SHARE_PREFIX = "SHARE_";
    public static final char PATH_SEPARATOR_CHAR = '/';
    public static final String PATH_SEPARATOR = "" + PATH_SEPARATOR_CHAR;

    @Autowired
    @Lazy
    public void setPathService(FilePathService pathService) {
        this.pathService = pathService;
    }

    protected void tryDeleteOrDeleteOnExit(File tmpFile) {
        if (!tmpFile.delete()) {
            log.error("delete tmp file error");
            tmpFile.deleteOnExit();
        }
    }

    private FilenameDescription getFilename(String name) {
        int i = name.lastIndexOf(".");
        String extName = null;
        String start = name;
        if (i != -1) {
            start = name.substring(0, i);
        }
        if (!(i == -1 || i == name.length() - 1)) {
            extName = name.substring(i + 1);
        }
        return new FilenameDescription(start, extName);
    }

    private <T> Optional<Result<T>> checkFilenameConflict(FilenameDescription filename, Long folder) {
        return checkFilenameConflict(filename.name, filename.ext, folder);
    }

    private <T> Optional<Result<T>> checkFilenameConflict(String start, String extName, Long folder) {
        if (fileMapper.selectCountByCondition(FILE_VO.NAME.eq(start).and(FILE_VO.EXT.eq(extName)).and(FILE_VO.FOLDER.eq(folder))) > 0) {
            return Optional.of(Result.failed(HttpStatus.CONFLICT, "存在同名文件，请改名或删除后重试！"));
        }
        return Optional.empty();
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
                FilenameDescription filename = getFilename(name);
                Optional<Result<FileVo>> illegalResult = filename.checkIllegal();
                if (illegalResult.isPresent()) {
                    return illegalResult.get();
                }
                String start = filename.name;
                String extName = filename.ext;
                InputStream inputStream = next.openStream();
                SizeStatisticsDigestInputStream digestInputStream = new SizeStatisticsDigestInputStream(inputStream, DigestUtils.getSha512Digest());
                File tmpFile = new File(savePath, "cache-" + randomUtils.generatorRandomFileName(20));
                try {
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
                } catch (Exception e) {
                    log.error("error while processing file", e);
                } finally {
                    tryDeleteOrDeleteOnExit(tmpFile);
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

    private QueryWrapper getRawFileSelector() {
        return select(
                FILE_VO.ID,
                FILE_VO.NAME,
                FILE_VO.EXT,
                FILE_VO.SIZE,
                FILE_VO.FOLDER.as("parent"),
                FILE_VO.UPLOADER.as("owner"),
                FILE_VO.UPLOAD_TIME.as("time"),
                string("FILE").as("type"),
                USER_VO.NAME.as("owner_name"),
                count(STATISTICS_VO.TIME).as("down_count")
        ).from(FILE_VO)
                .leftJoin(USER_VO).on(USER_VO.ID.eq(FILE_VO.UPLOADER).and(USER_VO.BANNED.eq(0)))
                .leftJoin(STATISTICS_VO).on(STATISTICS_VO_EXT.DOWNLOAD_FILE_ID.eq(FILE_VO.ID))
                .groupBy(FILE_VO.ID);
    }

    private QueryWrapper getRawFolderSelector() {
        return select(FOLDER_VO.ID,
                FOLDER_VO.NAME,
                null_().as("ext"),
                number(-1).as("size"),
                FOLDER_VO.PARENT,
                FOLDER_VO.CREATOR.as("owner"),
                FOLDER_VO.CREATE_TIME.as("time"),
                string("FOLDER").as("type"),
                USER_VO.NAME.as("owner_name"),
                number(0).as("down_count")
        ).from(FOLDER_VO)
                .leftJoin(USER_VO).on(USER_VO.ID.eq(FOLDER_VO.CREATOR).and(USER_VO.BANNED.eq(0)));
    }

    public Result<PageResult<FullRawFileObject>> getFilePager(long page, int num, long folder, SortField sort, boolean reverse, String keywords) {
        QueryWrapper rawFileSelect = getRawFileSelector();
        QueryWrapper rawFolderSelect = getRawFolderSelector();
        boolean queryFolder = true;
        if (keywords.isEmpty()) {
            rawFileSelect = rawFileSelect.where(FILE_VO.FOLDER.eq(folder));
            rawFolderSelect = rawFolderSelect.where(FOLDER_VO.PARENT.eq(folder));
        } else {
            FilenameDescription filename = getFilename(keywords);
            if (filename.ext.isEmpty()) {
                rawFileSelect = rawFileSelect.where(FILE_VO.NAME.like("%" + keywords + "%").or(FILE_VO.EXT.like("%" + keywords + "%")));
                rawFolderSelect.where(FOLDER_VO.NAME.like("%" + keywords + "%"));
            } else {
                queryFolder = false;
                rawFileSelect = rawFileSelect.where(FILE_VO.NAME.like("%" + filename.name + "%").and(FILE_VO.EXT.like("%" + filename + "%")));
            }
        }
        if (queryFolder) {
            rawFileSelect = rawFileSelect.unionAll(rawFolderSelect);
        }
        QueryWrapper from = new QueryWrapper().with("RAW_FILE").asSelect(rawFileSelect).select().from("RAW_FILE");
        if (sort != SortField.ID) {
            from = from.orderBy(sort.column, reverse);
        }
        from = from.orderBy(column("id"), reverse);
        long size = fileMapper.selectCountByQuery(selectCount().from(rawFileSelect).as("subQuery"));
        Page<FullRawFileObject> paginate = fileMapper.paginateAs(page, num, size, from, FullRawFileObject.class);
        PageResult<FullRawFileObject> result = new PageResult<>(paginate.getTotalRow(), paginate.getRecords());
        return Result.success(result);
    }


    public Result<FolderVo> mkdir(FolderCreateRequest request, UserVo user, String address) {
        String name = request.getName();
        long parent = request.getParent();
        if (name.isEmpty()) {
            return Result.failed(HttpStatus.BAD_REQUEST, "文件名不可为空！");
        }
        if (name.length() > 160) {
            return Result.failed(HttpStatus.PAYLOAD_TOO_LARGE, "文件夹名称过长，无法创建！");
        }
        if (fileMapper.selectCountByCondition(FILE_VO.EXT.eq(null_()).and(FILE_VO.NAME.eq(name)).and(FILE_VO.FOLDER.eq(parent))) > 0) {
            return Result.failed(HttpStatus.CONFLICT, "创建文件夹失败，同名文件已存在！");
        }
        FolderVo folder = folderMapper.selectOneByCondition(FOLDER_VO.NAME.eq(name).and(FOLDER_VO.PARENT.eq(parent)));
        if (folder != null) {
            if (request.isExistsReturn()) {
                return Result.success("文件夹已存在！", folder);
            }
            return Result.failed(HttpStatus.CONFLICT, "创建文件夹失败，同名文件夹已存在！");
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

    public void deleteFolder(long id) {
        fileMapper.selectListByCondition(FILE_VO.FOLDER.eq(id)).forEach(this::deleteFile);
        fileMapper.deleteByCondition(FILE_VO.FOLDER.eq(id));
        folderMapper.selectListByCondition(FOLDER_VO.PARENT.eq(id)).stream().map(FolderVo::getId).forEach(this::deleteFolder);
        folderMapper.deleteById(id);
    }

    @Transactional
    public Result<Void> delete(long id, FileType type, UserVo user, String address) {
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
                return Result.failed(HttpStatus.UNAUTHORIZED, "权限不足！");
            }
            this.deleteFolder(folder.getId());
        }
        statisticsService.insertAction(user, Actions.DELETE, JSONObject.of("id", id, "type", type, "address", address));
        return Result.success();
    }

    public void downloadFile(String id, String range, HttpServletResponse response) {
        FileVo fileVo = linkMapper.opsForValue().get(id);
        if (fileVo == null) {
            Result.failed(HttpStatus.NOT_FOUND, "未知文件！").writeToResponse(response);
            return;
        }
        long size = fileVo.getSize();
        String hash = fileVo.getHash();
        File file = new File(properties.getSavePath(), hash);
        List<ChunksVo> chunksVos = null;
        if (!file.exists()) {
            chunksVos = chunksMapper.selectListByQuery(select(CHUNKS_VO.ALL_COLUMNS).from(CHUNK_FILE_VO).rightJoin(CHUNKS_VO).on(CHUNK_FILE_VO.CHUNK_ID.eq(CHUNKS_VO.ID)).where(CHUNK_FILE_VO.FILE_ID.eq(fileVo.getId())).orderBy(CHUNK_FILE_VO.INDEX.asc()));
            if (chunksVos.isEmpty()) {
                Result.failed(HttpStatus.INTERNAL_SERVER_ERROR, "服务器错误，未找到文件，请联系服务器管理员处理！(shareLinkId=" + id + ", fileHash=" + hash + ", fileId=" + fileVo.getId() + ")").writeToResponse(response);
                return;
            }
        }
        long min = 0;
        long max = size - 1;
        if (!range.equals("null")) {
            String[] unitRanges = range.split("=");
            String[] minMax = unitRanges[1].split("-");
            if (minMax.length > 0) {
                min = Long.parseLong(minMax[0]);
            }
            if (minMax.length > 1) {
                max = Long.parseLong(minMax[1]);
            }
            response.setStatus(206);
            response.addHeader("Content-Range", "bytes " + min + "-" + max + "/" + size);
            response.addHeader("Accept-Ranges", "bytes");
        } else {
            response.setStatus(200);
        }
        log.debug("-------Prepare-Response-{}-{}-{}-------", min, max, id);
        String fullName = fileVo.getName();
        String ext = fileVo.getExt();
        if (ext != null) {
            fullName += '.' + ext;
        }
        response.addHeader("Content-Length", String.valueOf(max - min + 1));
        ContentDisposition disposition = ContentDisposition.attachment().filename(fullName, StandardCharsets.UTF_8).build();
        response.addHeader("Content-Disposition", disposition.toString());

        log.debug("-------Copy-{}-{}-{}-------", min, max, id);
        try (ServletOutputStream outputStream = response.getOutputStream()) {
            try (InputStream stream = chunksVos == null ? new FileInputStream(file) : openSerialFileInputStreamByChunks(chunksVos)) {
                StreamUtils.copyRange(stream, outputStream, min, max);
            }
        } catch (IOException e) {
            if (!response.isCommitted()) {
                response.reset();
            }
        }
        log.debug("-------flush-{}-{}-{}-------", min, max, id);
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

    public Result<FullRawFileObject> resolveFileDetail(String path) {
        String[] split = path.split(PATH_SEPARATOR);
        FullRawFileObject fileObj = pathService.resolveFile(split);
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

    public Result<FullRawFileObject> getFile(long id) {
        FullRawFileObject fileVo = this.fileMapper.selectOneByQueryAs(getRawFileSelector().where(FILE_VO.ID.eq(id)), FullRawFileObject.class);
        if (fileVo == null) {
            return Result.failed(HttpStatus.NOT_FOUND, "未知文件");
        }
        return Result.success(fileVo);
    }

    public Result<FullRawFileObject> getFolder(long id) {
        FullRawFileObject fileVo = this.folderMapper.selectOneByQueryAs(getRawFolderSelector().where(FOLDER_VO.ID.eq(id)), FullRawFileObject.class);
        if (fileVo == null) {
            return Result.failed(HttpStatus.NOT_FOUND, "未知文件");
        }
        return Result.success(fileVo);
    }

    public Result<FileVo> getFileDetail(long id) {
        FileVo fileVo = this.fileMapper.selectOneById(id);
        if (fileVo == null) {
            return Result.failed(HttpStatus.NOT_FOUND, "未知文件");
        }
        return Result.success(fileVo);
    }

    public Result<List<CheckChunkResult>> checkChunkUploaded(List<String> hash) {
        List<ChunksVo> chunksVos = chunksMapper.selectListByCondition(CHUNKS_VO.HASH.in(hash));
        Map<String, Long> hashResult = new HashMap<>();
        chunksVos.forEach(e -> hashResult.put(e.getHash(), e.getId()));
        List<CheckChunkResult> list1 = hash.stream().map(e -> new CheckChunkResult(e, hashResult.get(e))).toList();
        return Result.success(list1);
    }

    @SneakyThrows
    public Result<Long> uploadChunk(MultipartFile block) {
        File savePath = properties.getSavePath();
        File blobDir = new File(savePath, "blobs");
        SizeStatisticsDigestInputStream sizeStatisticsDigestInputStream = new SizeStatisticsDigestInputStream(block.getInputStream(), DigestUtils.getSha1Digest());
        byte[] bytes = sizeStatisticsDigestInputStream.readAllBytes();
        long size = sizeStatisticsDigestInputStream.getSize();
        sizeStatisticsDigestInputStream.close();
        MessageDigest messageDigest = sizeStatisticsDigestInputStream.getMessageDigest();
        String hex = HashUtil.toHex(messageDigest.digest());
        ChunksVo chunksVo = chunksMapper.selectOneByCondition(CHUNKS_VO.HASH.eq(hex));
        if (chunksVo != null) {
            return Result.success(chunksVo.getId());
        }
        String start = hex.substring(0, 2);
        File chunkBlockDir = new File(blobDir, start);
        if (!chunkBlockDir.exists()) {
            if (!chunkBlockDir.mkdirs()) {
                return Result.failed(HttpStatus.INTERNAL_SERVER_ERROR, "无法创建分区文件夹");
            }
        }
        File file = new File(chunkBlockDir, hex);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(bytes);
        }
        chunksVo = new ChunksVo();
        chunksVo.setHash(hex);
        chunksVo.setSize(size);
        chunksMapper.insert(chunksVo);
        return Result.success(chunksVo.getId());
    }

    @SneakyThrows
    @Transactional
    public Result<FileVo> saveFile(SaveChunksRequest chunks) {
        FilenameDescription filename = getFilename(chunks.getFilename());
        Optional<Result<FileVo>> illegalResult = filename.checkIllegal();
        if (illegalResult.isPresent()) {
            return illegalResult.get();
        }
        String name = filename.name();
        String ext = filename.ext();

        Long folderId = chunks.getFolderId();
        if (fileMapper.selectCountByCondition(FILE_VO.FOLDER.eq(folderId).and(FILE_VO.NAME.eq(name)).and(FILE_VO.EXT.eq(ext))) > 0) {
            return Result.failed(HttpStatus.CONFLICT, "文件已存在！");
        }
        FileVo fileVo = new FileVo();
        List<Long> chunkIds = chunks.getChunks();
        List<ChunksVo> chunksVos = chunksMapper.selectListByIds(chunkIds);
        List<ChunksVo> sortedChunks = chunkIds.stream().map(e -> chunksVos.stream().filter(a -> a.getId() == e).findFirst().orElseThrow()).toList();
        String mime;
        String sha512;
        long size;
        FileOutputStream fileOutputStream = new FileOutputStream("test.bin");
        try (SerialFileInputStream serialFileInputStream = openSerialFileInputStreamByChunks(sortedChunks)) {
            Tika tika = new Tika();
            mime = tika.detect(serialFileInputStream);
            serialFileInputStream.reset();
            try (SizeStatisticsDigestInputStream sizeStatisticsDigestInputStream = new SizeStatisticsDigestInputStream(serialFileInputStream, DigestUtils.getSha512Digest())) {
                sizeStatisticsDigestInputStream.transferTo(fileOutputStream);
                sizeStatisticsDigestInputStream.close();
                sha512 = HexUtils.toHexString(sizeStatisticsDigestInputStream.getMessageDigest().digest());
                size = sizeStatisticsDigestInputStream.getSize();
            }
        }
        fileOutputStream.close();

        fileVo.setName(name);
        fileVo.setExt(ext);
        fileVo.setUploader(-2);
        fileVo.setFolder(folderId);
        fileVo.setMime(mime);
        fileVo.setHash(sha512);
        fileVo.setSize(size);
        fileMapper.insert(fileVo);
        long fileId = fileVo.getId();
        AtomicLong currentIndex = new AtomicLong();
        List<ChunkFileVo> mapper = chunkIds.stream().map(e -> new ChunkFileVo(e, fileId, currentIndex.getAndIncrement())).toList();
        chunkFileMapper.insertBatch(mapper);
        return Result.success(fileVo);
    }

    private SerialFileInputStream openSerialFileInputStreamByChunks(List<ChunksVo> chunks) {
        List<File> list = chunks.stream().map(ChunksVo::getHash).map(e -> new File(new File(new File(properties.getSavePath(), "blobs"), e.substring(0, 2)), e)).toList();
        return new SerialFileInputStream(list);
    }

    public Result<Boolean> checkUploadPossible(String name, Long folder) {
        FilenameDescription filename = getFilename(name);
        Optional<Result<Boolean>> illegalResult = filename.checkIllegal();
        if (illegalResult.isPresent()) {
            return illegalResult.get();
        }
        Optional<Result<Boolean>> objectResult = this.checkFilenameConflict(filename, folder);
        return objectResult.orElseGet(() -> Result.success("可以上传", true));
    }

    public List<FullRawFileObject> getRawFilesByNameAndFolder(String name, String ext, long folder) {
        return fileMapper.selectListByQueryAs(getRawFileSelector().where(FILE_VO.NAME.eq(name).and(SortField.EXT.column.eq(ext)).and(FILE_VO.FOLDER.eq(folder)))
                .unionAll(getRawFolderSelector().where(FOLDER_VO.NAME.eq(name).and(FOLDER_VO.PARENT.eq(folder)))), FullRawFileObject.class);
    }

    private QueryWrapper getRawFilesByNameQuery(String name, String ext) {
        return getRawFileSelector().where(FILE_VO.NAME.eq(name).and(SortField.EXT.column.eq(ext)))
                .unionAll(getRawFolderSelector().where(FOLDER_VO.NAME.eq(name)));
    }

    public List<FullRawFileObject> getRawFilesByName(String name, String ext) {
        return fileMapper.selectListByQueryAs(this.getRawFilesByNameQuery(name, ext), FullRawFileObject.class);
    }

    public long getRawFilesCountByName(String name, String ext) {
        return fileMapper.selectCountByQuery(selectCount().from(this.getRawFilesByNameQuery(name, ext)).as("subQuery"));
    }

    public Result<Boolean> moveFile(MoveFileRequest request, UserVo user) {
        long originalFileId = request.getOriginalFileId();
        FileType fileType = request.getFileType();
        long userId = user.getId();
        boolean isAdmin = user.getAuth().equals(Auth.admin);
        if (fileType.equals(FileType.FILE)) {
            FileVo fileVo = fileMapper.selectOneById(originalFileId);
            if (fileVo == null) {
                return Result.failed(HttpStatus.NOT_FOUND, "原始文件不存在！");
            }
            if (!isAdmin && fileVo.getUploader() != userId) {
                return Result.failed(HttpStatus.FORBIDDEN, "权限不足！");
            }
            String newFilename = request.getNewFilename();
            if (newFilename == null) {
                String ext = fileVo.getExt();
                newFilename = fileVo.getName() + (ext.isEmpty() ? "" : "." + ext);
            }
            Long newParentId = request.getNewParentId();
            if (newParentId == null) {
                newParentId = fileVo.getFolder();
            }
            if (fileMapper.selectCountByCondition(FILE_VO.NAME.eq(newFilename).and(FILE_VO.FOLDER.eq(newParentId))) > 0) {
                return Result.failed(HttpStatus.CONFLICT, "文件已存在！");
            }
            FileVo targetVo = new FileVo();
            targetVo.setId(originalFileId);
            targetVo.setName(newFilename);
            targetVo.setFolder(newParentId);
            fileMapper.update(targetVo);
            return Result.success("移动成功");
        }
        FolderVo folderVo = folderMapper.selectOneById(originalFileId);
        if (folderVo == null) {
            return Result.failed(HttpStatus.NOT_FOUND, "原始文件不存在！");
        }
        if (!isAdmin) {
            return Result.failed(HttpStatus.FORBIDDEN, "权限不足！");
        }
        String newFilename = request.getNewFilename();
        if (newFilename == null) {
            newFilename = folderVo.getName();
        }
        Long newParentId = request.getNewParentId();
        if (newParentId == null) {
            newParentId = folderVo.getParent();
        }
        if (folderMapper.selectCountByCondition(FOLDER_VO.NAME.eq(newFilename).and(FOLDER_VO.PARENT.eq(newParentId))) > 0) {
            return Result.failed(HttpStatus.CONFLICT, "文件已存在！");
        }
        FolderVo targetVo = new FolderVo();
        targetVo.setId(originalFileId);
        targetVo.setName(newFilename);
        targetVo.setParent(newParentId);
        folderMapper.update(targetVo);
        return Result.success("移动成功");
    }

    private record FilenameDescription(String name, String ext) {
        private FilenameDescription(String name, String ext) {
            this.name = name;
            if (ext == null) {
                this.ext = "";
                return;
            }
            this.ext = ext;
        }

        public <T> Optional<Result<T>> checkIllegal() {
            if (name.length() > 120 || ext.length() > 40) {
                return Optional.of(Result.failed(HttpStatus.PAYLOAD_TOO_LARGE, "文件名过长，无法上传！"));
            }
            if (name.isEmpty()) {
                return Optional.of(Result.failed(HttpStatus.BAD_REQUEST, "文件名为空，无法上传！"));
            }
            return Optional.empty();
        }
    }
}