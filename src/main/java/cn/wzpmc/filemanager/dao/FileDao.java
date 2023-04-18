package cn.wzpmc.filemanager.dao;

import cn.wzpmc.filemanager.entities.FileObject;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FileDao {
    Long getFileCount();
    List<FileObject> getFiles(@Param("id") int id);
    int countSearchFilesByName(@Param("keywords") String keywords);
    int countSearchFilesByMd5(@Param("keywords") String keywords);
    int countSearchFilesByFormat(@Param("keywords") String keywords);
    List<FileObject> searchFilesById(@Param("keywords") Integer keywords,@Param("id") int id);
    List<FileObject> searchFilesByName(@Param("keywords") String keywords,@Param("id") int id);
    List<FileObject> searchFilesByMd5(@Param("keywords") String keywords,@Param("id") int id);
    List<FileObject> searchFilesByFormat(@Param("keywords") String keywords,@Param("id") int id);
    FileObject getFileInfo(@Param("id") int id);
    FileObject getFullFileInfo(@Param("id") int id);
    void uploadFile(FileObject fileObject);
    void deleteFile(@Param("id") int id);
}
