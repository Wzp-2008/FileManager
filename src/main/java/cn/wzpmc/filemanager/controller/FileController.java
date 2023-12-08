package cn.wzpmc.filemanager.controller;

import cn.wzpmc.filemanager.entities.FileObject;
import cn.wzpmc.filemanager.entities.Page;
import cn.wzpmc.filemanager.entities.Result;
import cn.wzpmc.filemanager.entities.vo.FileObjectVo;
import cn.wzpmc.filemanager.enums.SearchType;
import cn.wzpmc.filemanager.service.FileService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/file")
public class FileController {
    private final FileService service;
    @Autowired
    public FileController(FileService service){
        this.service = service;
    }
    @GetMapping("/checkUpload")
    public Result<Boolean> checkUpload(@RequestParam("filename") String filename){
        return service.checkUpload(filename);
    }
    @PostMapping("/upload")
    public Result<FileObjectVo> uploadFile(@RequestBody MultipartFile file, @RequestHeader("Authorization") String authorization){
        return service.uploadFile(file, authorization);
    }
    @GetMapping("/link")
    public Result<String> generatorDownloadLink(@RequestParam("id") int id, HttpServletRequest request){
        return service.generatorDownloadLink(id, request);
    }
    @GetMapping("/download/{fileLink}")
    public void downloadFile(@PathVariable("fileLink") String link, HttpServletResponse response) throws IOException {
        service.downloadFile(link, response);
    }
    @GetMapping("/getAll")
    public Result<Page<FileObjectVo>> getAllFile(@RequestParam("page") int page, @RequestParam("num") int num){
        return service.getAllFile(page, num);
    }
    @PostMapping("/remove")
    public Result<Boolean> removeFile(@RequestBody FileObject fileObject, @RequestHeader("Authorization") String authorization) {
        return service.removeFile(fileObject.getId(), authorization);
    }
    @GetMapping("/search")
    public Result<Page<FileObjectVo>> searchFile(@RequestParam("page") int page, @RequestParam("num") int num, @RequestParam("type") SearchType type, @RequestParam("data") String data) {
        return service.search(page, num, type, data);
    }
}
