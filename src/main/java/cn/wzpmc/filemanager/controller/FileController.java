package cn.wzpmc.filemanager.controller;

import cn.wzpmc.filemanager.annotation.AuthorizationRequired;
import cn.wzpmc.filemanager.entities.Result;
import cn.wzpmc.filemanager.entities.files.CheckChunkResponse;
import cn.wzpmc.filemanager.entities.files.DoneUploadRequest;
import cn.wzpmc.filemanager.entities.files.FileObject;
import cn.wzpmc.filemanager.entities.files.PrepareUploadRequest;
import cn.wzpmc.filemanager.entities.vo.UserVo;
import cn.wzpmc.filemanager.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/file")
public class FileController {
    private final FileService fileService;

    @Autowired
    public FileController(FileService fileService) {
        this.fileService = fileService;
    }
    @PostMapping("/prepare")
    public Result<String> prepareUploadChunks(@RequestBody PrepareUploadRequest prepareUploadRequest, @AuthorizationRequired UserVo user){
        return fileService.prepareUploadChunks(prepareUploadRequest, user);
    }
    @PutMapping("/chunk/upload")
    public Result<Void> uploadChunk(MultipartFile file,@RequestParam String id){
        return fileService.uploadChunk(file, id);
    }
    @GetMapping("/chunk/check")
    public Result<CheckChunkResponse> checkChunk(@RequestParam String hash, @RequestParam String id, @RequestParam long index){
        return fileService.checkChunk(hash, id, index);
    }
    @PostMapping("/done")
    public Result<FileObject> doneUpload(@RequestBody DoneUploadRequest data){
        return fileService.doneUpload(data.getId());
    }
}