package cn.wzpmc.filemanager.controller;

import cn.wzpmc.filemanager.annotation.Address;
import cn.wzpmc.filemanager.annotation.AuthorizationRequired;
import cn.wzpmc.filemanager.entities.PageResult;
import cn.wzpmc.filemanager.entities.Result;
import cn.wzpmc.filemanager.entities.files.DeleteRequest;
import cn.wzpmc.filemanager.entities.files.FolderCreateRequest;
import cn.wzpmc.filemanager.entities.files.RawFileObject;
import cn.wzpmc.filemanager.entities.files.enums.FileType;
import cn.wzpmc.filemanager.entities.vo.FileVo;
import cn.wzpmc.filemanager.entities.vo.FolderVo;
import cn.wzpmc.filemanager.entities.vo.UserVo;
import cn.wzpmc.filemanager.service.FileService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.util.Date;

@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
public class FileController {
    private final FileService fileService;

    @PutMapping("/upload")
    public Result<FileVo> simpleUpload(MultipartHttpServletRequest file, @AuthorizationRequired UserVo user, @Address String address) {
        return fileService.simpleUpload(file, user, address);
    }

    @GetMapping("/get")
    public Result<PageResult<RawFileObject>> getFilePager(@RequestParam long page, @RequestParam int num, @RequestParam long folder, @Address String address) {
        return fileService.getFilePager(page, num, folder, address);
    }

    @PostMapping("/mkdir")
    public Result<FolderVo> mkdir(@RequestBody FolderCreateRequest request, @AuthorizationRequired UserVo user, @Address String address) {
        return fileService.mkdir(request, user, address);
    }

    @GetMapping("/detail")
    public Result<FileVo> getFileDetail(@RequestParam long id) {
        return fileService.getFileDetail(id);
    }

    @DeleteMapping("/rm")
    public Result<Void> delete(@RequestBody DeleteRequest request, @AuthorizationRequired UserVo user, @Address String address) {
        return fileService.delete(request, user, address);
    }

    @GetMapping("/link")
    public Result<String> getFileLink(@RequestParam long id, @Address String address, HttpServletRequest request) {
        return fileService.getFileLink(id, address, request);
    }

    @GetMapping("/download/{id}")
    public void downloadFile(@PathVariable String id, @RequestHeader(value = "Range", defaultValue = "null") String range, HttpServletResponse response) {
        fileService.downloadFile(id, range, response);
    }

    @GetMapping("/share")
    public Result<String> shareFile(@RequestParam Long id, @RequestParam(defaultValue = "9999-12-31") Date lastCouldDownloadTime, @RequestParam(defaultValue = "-1") int maxDownloadCount) {
        return fileService.shareFile(id, lastCouldDownloadTime, maxDownloadCount);
    }

    @GetMapping("/path/resolve")
    public Result<RawFileObject> resolveFileDetail(@RequestParam String path) {
        return fileService.resolveFileDetail(path);
    }

    @GetMapping("/path/{id}")
    public Result<String> findFilePathById(@PathVariable("id") long id, @RequestParam(value = "type", defaultValue = "FILE") FileType type) {
        return type.equals(FileType.FILE) ? fileService.findFilePathById(id) : fileService.findFolderPathById(id);
    }
}