package cn.wzpmc.filemanager.mapper;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface InitializationMapper {
    void createFileTable();
    void createFolderTable();
    void createStatisticsTable();
    void createUserTable();
    void createPrefTable();
    void createFingerprintTable();
}
