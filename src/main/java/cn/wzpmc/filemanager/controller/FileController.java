package cn.wzpmc.filemanager.controller;

import cn.wzpmc.filemanager.entities.FileObject;
import cn.wzpmc.filemanager.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
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

}
