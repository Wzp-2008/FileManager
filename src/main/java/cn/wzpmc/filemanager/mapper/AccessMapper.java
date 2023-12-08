package cn.wzpmc.filemanager.mapper;

import cn.wzpmc.filemanager.entities.AccessInformation;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AccessMapper {
    void createDefault();
    int countToday();
    void addTodayAccess();
    void addTodayDownload();
    void addToday();
    List<AccessInformation> getAllAccessData(int count);
    AccessInformation getAllAccessInformation();
}
