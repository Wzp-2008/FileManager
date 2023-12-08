package cn.wzpmc.filemanager.controller;

import cn.wzpmc.filemanager.entities.AccessInformation;
import cn.wzpmc.filemanager.service.AccessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/access")
public class AccessController {
    private final AccessService service;
    @Autowired
    public AccessController(AccessService service){
        this.service = service;
    }
    @GetMapping("/get")
    public List<AccessInformation> getAccessInformation(@RequestParam("count") int count){
        return this.service.getAccessInformation(count);
    }
    @GetMapping("/getAll")
    public AccessInformation getAllAccessInformation(){
        return this.service.getAllAccessInformation();
    }
}
