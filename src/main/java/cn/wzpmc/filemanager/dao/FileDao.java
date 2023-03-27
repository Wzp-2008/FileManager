package cn.wzpmc.filemanager.dao;

import cn.wzpmc.filemanager.entities.FileObject;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FileDao {
    Long getFileCount();

    List<FileObject> getFiles(@Param("id") int id);
}
