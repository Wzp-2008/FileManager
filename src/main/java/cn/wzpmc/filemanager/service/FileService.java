package cn.wzpmc.filemanager.service;

import cn.wzpmc.filemanager.dao.FileDao;
import cn.wzpmc.filemanager.entities.FileObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class FileService {
    private final FileDao dao;
    @Autowired
    public FileService(FileDao dao){
        this.dao = dao;
    }

    public Long getFileCount() {
        return dao.getFileCount();
    }

    public List<FileObject> getFiles(int page) {
        if (page <= 0){
            return new ArrayList<>();
        }
        return dao.getFiles((page - 1) * 20);
    }
}
