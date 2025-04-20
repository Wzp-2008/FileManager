package cn.wzpmc.filemanager.controller;

import cn.wzpmc.filemanager.annotation.Address;
import cn.wzpmc.filemanager.annotation.AuthorizationRequired;
import cn.wzpmc.filemanager.entities.PageResult;
import cn.wzpmc.filemanager.entities.Result;
import cn.wzpmc.filemanager.entities.chunk.CheckChunkResult;
import cn.wzpmc.filemanager.entities.chunk.SaveChunksRequest;
import cn.wzpmc.filemanager.entities.files.FolderCreateRequest;
import cn.wzpmc.filemanager.entities.files.FullRawFileObject;
import cn.wzpmc.filemanager.entities.files.enums.FileType;
import cn.wzpmc.filemanager.entities.files.enums.SortField;
import cn.wzpmc.filemanager.entities.vo.FileVo;
import cn.wzpmc.filemanager.entities.vo.FolderVo;
import cn.wzpmc.filemanager.entities.vo.UserVo;
import cn.wzpmc.filemanager.service.FileService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.util.Date;
import java.util.List;

/**
 * 文件操作相关接口
 */
@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
public class FileController {
    private final FileService fileService;

    /**
     * 上传一个文件
     * @param file Multipart格式的文件对象
     * @return 文件详情
     */
    @PutMapping("/upload")
    public Result<FileVo> simpleUpload(MultipartHttpServletRequest file, @AuthorizationRequired UserVo user, @Address String address) {
        return fileService.simpleUpload(file, user, address);
    }

    /**
     * 分页获取文件
     * @param page 要获取第几页的文件
     * @param num 每一页的文件数量
     * @param folder 要获取的文件所在的文件夹
     * @param sort 文件的排序方式
     * @param reverse 是否反向排序
     * @return 分页后的文件列表
     */
    @GetMapping("/get")
    public Result<PageResult<FullRawFileObject>> getFilePager(@RequestParam long page, @RequestParam int num, @RequestParam long folder, @RequestParam(defaultValue = "TIME") SortField sort, @RequestParam(defaultValue = "false") boolean reverse) {
        return fileService.getFilePager(page, num, folder, sort, reverse);
    }

    /**
     * 创建一个文件夹
     * @param request 创建文件夹的相关参数
     * @return 创建的文件夹详情
     */
    @PostMapping("/mkdir")
    public Result<FolderVo> mkdir(@RequestBody FolderCreateRequest request, @AuthorizationRequired UserVo user, @Address String address) {
        return fileService.mkdir(request, user, address);
    }

    /**
     * 获取一个文件的简略信息
     * @param id 文件ID
     * @return 文件简略信息
     */
    @GetMapping("/get/file")
    public Result<FullRawFileObject> getFile(@RequestParam long id) {
        return fileService.getFile(id);
    }

    /**
     * 获取一个文件夹的简略信息
     * @param id 文件夹ID
     * @return 文件夹简略信息
     */
    @GetMapping("/get/folder")
    public Result<FullRawFileObject> getFolder(@RequestParam long id) {
        return fileService.getFolder(id);
    }

    /**
     * 获取文件详细信息
     * @param id 文件ID
     * @return 文件详细信息
     */
    @GetMapping("/detail/file")
    public Result<FileVo> getFileDetail(@RequestParam long id) {
        return fileService.getFileDetail(id);
    }

    /**
     * 删除一个文件/文件夹
     * @param id 文件/文件夹ID
     * @param type 目标为文件/文件夹
     * @return 是否删除成功
     */
    @DeleteMapping("/rm")
    public Result<Void> delete(@RequestParam long id, @RequestParam FileType type, @AuthorizationRequired UserVo user, @Address String address) {
        return fileService.delete(id, type, user, address);
    }

    /**
     * 获取文件下载链接
     * @param id 文件ID
     * @return 文件下载链接ID
     */
    @GetMapping("/link")
    public Result<String> getFileLink(@RequestParam long id, @Address String address, HttpServletRequest request) {
        return fileService.getFileLink(id, address, request);
    }

    /**
     * 通过下载文件ID下载文件
     * @param id 下载ID
     * @see #getFileLink(long, String, HttpServletRequest)
     */
    @GetMapping("/download/{id}")
    public void downloadFile(@PathVariable String id, @RequestHeader(value = "Range", defaultValue = "null") String range, HttpServletResponse response) {
        fileService.downloadFile(id, range, response);
    }

    /**
     * 分享文件
     * @deprecated
     */
    @GetMapping("/share")
    @Deprecated
    public Result<String> shareFile(@RequestParam Long id, @RequestParam(defaultValue = "9999-12-31") Date lastCouldDownloadTime, @RequestParam(defaultValue = "-1") int maxDownloadCount) {
        return fileService.shareFile(id, lastCouldDownloadTime, maxDownloadCount);
    }

    /**
     * 通过路径解析文件信息
     * @param path 需要解析的文件路径
     * @return 文件粗略信息
     */
    @GetMapping("/path/resolve")
    public Result<FullRawFileObject> resolveFileDetail(@RequestParam String path) {
        return fileService.resolveFileDetail(path);
    }

    /**
     * 通过文件ID获取文件路径
     * @param id 文件ID
     * @param type 目标文件为文件/文件夹
     * @return 文件的路径
     */
    @GetMapping("/path/{id}")
    public Result<String> findFilePathById(@PathVariable("id") long id, @RequestParam(value = "type", defaultValue = "FILE") FileType type) {
        return type.equals(FileType.FILE) ? fileService.findFilePathById(id) : fileService.findFolderPathById(id);
    }

    @PostMapping("/chunk/check")
    public Result<List<CheckChunkResult>> checkChunkUploaded(@RequestBody List<String> hash) {
        return fileService.checkChunkUploaded(hash);
    }

    @PostMapping("/chunk/upload")
    public Result<Long> uploadChunk(@RequestBody MultipartFile block) {
        return fileService.uploadChunk(block);
    }

    @PutMapping("/chunk/save")
    public Result<FileVo> saveFile(@RequestBody SaveChunksRequest request) {
        return fileService.saveFile(request);
    }
}