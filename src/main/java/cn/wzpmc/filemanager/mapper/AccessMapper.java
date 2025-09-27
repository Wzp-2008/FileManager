package cn.wzpmc.filemanager.mapper;

import cn.wzpmc.filemanager.entities.AccessInformation;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component
public interface AccessMapper {
    void createDefault();
    int countToday();
    void addTodayAccess();
    void addTodayDownload();
    void addToday();
    List<AccessInformation> getAllAccessData(int count);
    AccessInformation getAllAccessInformation();
}