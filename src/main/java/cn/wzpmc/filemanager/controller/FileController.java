package cn.wzpmc.filemanager.controller;

import cn.wzpmc.filemanager.entities.CountableList;
import cn.wzpmc.filemanager.entities.FileObject;
import cn.wzpmc.filemanager.enums.SearchType;
import cn.wzpmc.filemanager.service.FileService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
public class FileController {
    private final FileService service;
    @Autowired
    public FileController(FileService service){
        this.service = service;
    }
    @GetMapping("/api/file/count")
    public Long getFileCount(){
        return service.getFileCount();
    }
    @GetMapping("/api/file/get")
    public List<FileObject> getFiles(@RequestParam("page") int page){
        return service.getFiles(page);
    }
    @GetMapping("/api/file/search")
    public CountableList<FileObject> searchFiles(@RequestParam("type") SearchType type, @RequestParam("keywords") String keywords, @RequestParam("page") int page){
        return switch (type){
            case NAME -> service.searchFilesByName(keywords, page);
            case ID -> service.searchFilesById(Integer.parseInt(keywords), page);
            case MD5 -> service.searchFilesByMd5(keywords, page);
            case FORMAT -> service.searchFilesByFormat(keywords, page);
        };
    }
    @GetMapping("/api/file")
    public void downloadFile(@RequestParam("id") int id, HttpServletResponse response){
        service.downloadFile(id, response);
    }
    @PostMapping("/api/file/upload")
    public FileObject uploadFile(@RequestHeader("Authorization") String token, @RequestBody MultipartFile file){
        return service.uploadFile(token, file);
    }
    @PostMapping("/api/file/remove")
    public boolean removeFile(@RequestHeader("Authorization") String token, @RequestBody FileObject file){
        return service.removeFile(token, file);
    }
    @GetMapping("/api/file/details")
    public Map<String, Object> getFileDetails(@RequestParam("id") int id){
        return service.getFileDetails(id);
    }
}
