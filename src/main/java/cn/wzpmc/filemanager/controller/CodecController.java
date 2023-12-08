package cn.wzpmc.filemanager.controller;

import cn.wzpmc.filemanager.entities.Result;
import cn.wzpmc.filemanager.service.CodecService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/codec")
public class CodecController {
    private final CodecService service;
    @Autowired
    public CodecController(CodecService service){
        this.service = service;
    }
    @GetMapping("/test")
    public Result<String> getFileType(@RequestParam("id") int id){
        return this.service.getFileType(id);
    }
}
