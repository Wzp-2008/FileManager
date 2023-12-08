package cn.wzpmc.filemanager.mapper;

import cn.wzpmc.filemanager.entities.FileObject;
import cn.wzpmc.filemanager.entities.vo.FileObjectVo;
import org.apache.ibatis.annotations.Mapper;

import java.util.Date;
import java.util.List;

@Mapper
public interface FileMapper {
    void createDefault();
    void addFile(FileObject file);
    FileObject getOriginalFile(int id);
    FileObjectVo getFileVo(long id);
    int getFileCountByNameAndType(String name, String type);
    FileObject getFileById(long id);
    void addDownloadCount(int id);
    int getAllFileCount();
    List<FileObjectVo> getPageFile(int minId, int num);
    void removeFile(int id);
    String getHashById(int id);
    List<FileObjectVo> getFileByType(String type, int minId, int num);
    int getFileCountByType(String type);
    List<FileObjectVo> getFileByUploader(String uploader, int minId, int num);
    int getFileCountByUploader(String uploader);
    List<FileObjectVo> getFileByName(String name, int minId, int num);
    int getFileCountByName(String name);
    List<FileObjectVo> getFileByDate(Date date, int minId, int num);
    int getFileCountByDate(Date date);
}
