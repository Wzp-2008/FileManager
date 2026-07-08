package cn.wzpmc.filemanager.mapper;

import org.apache.ibatis.annotations.Mapper;

/**
 * 表初始化Mapper
 */
@Mapper
public interface InitializationMapper {
    void createFileTable();

    void createFolderTable();

    void createStatisticsTable();

    void createUserTable();

    void createPrefTable();

    void createFingerprintTable();

    void createChunksTable();

    void createChunkFileTable();

    boolean checkUserPasswordIsSHA1();
}
