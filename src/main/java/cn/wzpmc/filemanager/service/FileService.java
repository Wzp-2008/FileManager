package cn.wzpmc.filemanager.service;

import cn.wzpmc.filemanager.config.FileManagerProperties;
import cn.wzpmc.filemanager.entities.Result;
import cn.wzpmc.filemanager.entities.files.*;
import cn.wzpmc.filemanager.entities.vo.ChunkVo;
import cn.wzpmc.filemanager.entities.vo.FileChunkVo;
import cn.wzpmc.filemanager.entities.vo.FileVo;
import cn.wzpmc.filemanager.entities.vo.UserVo;
import cn.wzpmc.filemanager.mapper.ChunkMapper;
import cn.wzpmc.filemanager.mapper.FileChunksMapper;
import cn.wzpmc.filemanager.mapper.FileMapper;
import cn.wzpmc.filemanager.mapper.FolderMapper;
import cn.wzpmc.filemanager.utils.RandomUtils;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import static cn.wzpmc.filemanager.entities.vo.table.ChunkVoTableDef.CHUNK_VO;
import static cn.wzpmc.filemanager.entities.vo.table.FileChunkVoTableDef.FILE_CHUNK_VO;
import static cn.wzpmc.filemanager.entities.vo.table.FileVoTableDef.FILE_VO;

@Service
@RequiredArgsConstructor
public class FileService {
    private final RedisTemplate<String, ChunkReady> uploadMapper;
    private final RedisTemplate<String, ChunkChecked> chunkUploadMapper;
    private final FileMapper fileMapper;
    private final ChunkMapper chunkMapper;
    private final FileChunksMapper fileChunksMapper;
    private final FolderMapper folderMapper;
    private final RandomUtils randomUtils;
    private final FileManagerProperties properties;
    private static final String UPLOAD_FILE_PREPARE_HEAD = "UPLOAD_";
    private static final String CHUNK_PREPARE_HEAD = "UPLOAD_CHUNK_";
    public Result<String> prepareUploadChunks(PrepareUploadRequest prepareUploadRequest, UserVo user) {
        String name = prepareUploadRequest.getName();
        String ext = prepareUploadRequest.getExt();
        int folder = prepareUploadRequest.getFolder();
        if (this.fileMapper.selectCountByCondition(FILE_VO.NAME.eq(name).and(FILE_VO.EXT.eq(ext)).and(FILE_VO.FOLDER.eq(folder))) > 0) {
            return Result.failed(HttpStatus.CONFLICT, "文件已存在！");
        }
        String fullSha1 = prepareUploadRequest.getFullSha1();
        FileVo otherSameFile = this.fileMapper.selectOneByCondition(FILE_VO.SHA1.eq(fullSha1));
        ChunkReady chunkReady = new ChunkReady();
        FileVo fileVo = new FileVo();
        fileVo.setUploader(user.getId());
        fileVo.setName(name);
        fileVo.setExt(ext);
        fileVo.setFolder(folder);
        fileVo.setSha1(fullSha1);
        this.fileMapper.insert(fileVo);
        int fid = fileVo.getId();
        if (otherSameFile != null) {
            int id = otherSameFile.getId();
            List<FileChunkVo> fileChunkVos = this.fileChunksMapper.selectListByCondition(FILE_CHUNK_VO.FILE.eq(id));
            for (FileChunkVo fileChunkVo : fileChunkVos) {
                fileChunkVo.setFile(fid);
                this.fileChunksMapper.insert(fileChunkVo);
            }
            return Result.failed(HttpStatus.FOUND, "后台存在相同文件，无需上传！");
        }
        String uploadId = this.randomUtils.generatorRandomString(40);
        chunkReady.setFileId(fid);
        chunkReady.setLength(prepareUploadRequest.getSize());
        uploadMapper.opsForValue().set(UPLOAD_FILE_PREPARE_HEAD + uploadId, chunkReady);
        return Result.success("成功", uploadId);
    }

    @SneakyThrows
    @Transactional
    public Result<Void> uploadChunk(MultipartFile file, String id) {
        ValueOperations<String, ChunkChecked> chunkOps  = chunkUploadMapper.opsForValue();
        ChunkChecked chunkData = chunkOps.getAndDelete(CHUNK_PREPARE_HEAD + id);
        if (chunkData == null) {
            return Result.failed(HttpStatus.NOT_FOUND, "未知的文件块");
        }
        long size = file.getSize();
        if (size > 64 * 1024 * 1024) {
            return Result.failed(HttpStatus.PAYLOAD_TOO_LARGE, "文件块不应大于64MB");
        }
        byte[] bytes = file.getBytes();
        String s = DigestUtils.sha1Hex(bytes);
        if (!s.equals(chunkData.getHash())) {
            return Result.failed(HttpStatus.CONFLICT, "文件块内容错误！");
        }
        ChunkVo chunkVo = new ChunkVo();
        chunkVo.setSize(size);
        chunkVo.setSha1(s);
        String hashHead = s.substring(0, 2);
        File hashHeadFolder = new File(properties.getSavePath(), hashHead);
        if (!hashHeadFolder.exists()) {
            if (!hashHeadFolder.mkdirs()) {
                return Result.failed(HttpStatus.INTERNAL_SERVER_ERROR, "写入文件块出现错误，创建文件夹失败");
            }
        }
        File chunkFile = new File(hashHeadFolder, s);
        if (!chunkFile.createNewFile()) {
            return Result.failed(HttpStatus.INTERNAL_SERVER_ERROR, "写入文件块出现错误，无法写入块文件");
        }
        try(FileOutputStream fos = new FileOutputStream(chunkFile)) {
            fos.write(bytes);
        }
        String fileId = chunkData.getFileId();
        ChunkReady chunkReady = uploadMapper.opsForValue().get(UPLOAD_FILE_PREPARE_HEAD + fileId);
        assert chunkReady != null;
        long fileTableId = chunkReady.getFileId();
        this.chunkMapper.insert(chunkVo);
        int chunkId = chunkVo.getId();
        FileChunkVo fileChunkVo = new FileChunkVo(fileTableId, chunkId, chunkData.getIndex());
        this.fileChunksMapper.insert(fileChunkVo);
        return Result.success("成功");
    }

    public Result<CheckChunkResponse> checkChunk(String hash, String id, long index) {
        ChunkReady chunkReady = uploadMapper.opsForValue().get(UPLOAD_FILE_PREPARE_HEAD + id);
        if (chunkReady == null) {
            return Result.failed(HttpStatus.NOT_FOUND, "未知的文件ID！");
        }
        long fileId = chunkReady.getFileId();
        ChunkVo chunkVo = chunkMapper.selectOneByCondition(CHUNK_VO.SHA1.eq(hash));
        if (chunkVo != null) {
            FileChunkVo fileChunkVo = new FileChunkVo(fileId, chunkVo.getId(), index);
            this.fileChunksMapper.insert(fileChunkVo);
            return Result.success(CheckChunkResponse.has());
        }
        ValueOperations<String, ChunkChecked> chunkOps = chunkUploadMapper.opsForValue();
        String chunkId = randomUtils.generatorRandomString(40);
        chunkOps.set(CHUNK_PREPARE_HEAD + chunkId, new ChunkChecked(id, hash, index));
        return Result.success(CheckChunkResponse.shouldUpload(chunkId));
    }

    public Result<FileObject> doneUpload(String id) {
        ChunkReady andDelete = uploadMapper.opsForValue().getAndDelete(UPLOAD_FILE_PREPARE_HEAD + id);
        if (andDelete == null) {
            return Result.failed(HttpStatus.NOT_FOUND, "未知的文件ID");
        }
        long totalLength = andDelete.getLength();
        long l = calcFileSize(andDelete.getFileId());
        if (l != totalLength) {
            fileMapper.deleteById(andDelete.getFileId());
            return Result.failed(HttpStatus.LENGTH_REQUIRED, "应收到" + totalLength + "字节，但只收到" + l + "字节！");
        }
        return Result.success();
    }
    private long calcFileSize(long id) {
        List<Long> longs = this.fileChunksMapper.selectListByQueryAs(new QueryWrapper().select(CHUNK_VO.SIZE).from(FILE_CHUNK_VO).where(FILE_CHUNK_VO.FILE.eq(id)).leftJoin(CHUNK_VO).on(CHUNK_VO.ID.eq(FILE_CHUNK_VO.CHUNK)), Long.class);
        long sum = 0L;
        for (Long aLong : longs) {
            sum += aLong;
        }
        return sum;
    }
}