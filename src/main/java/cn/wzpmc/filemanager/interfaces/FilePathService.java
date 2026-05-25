package cn.wzpmc.filemanager.interfaces;

import cn.wzpmc.filemanager.entities.files.FullRawFileObject;
import cn.wzpmc.filemanager.entities.files.RawFileObject;
import org.jspecify.annotations.NonNull;
import org.springframework.lang.Nullable;

/**
 * 文件路径服务接口（需实现）
 */
public interface FilePathService {
    /**
     * 通过文件/文件夹获取路径
     *
     * @param file 文件/文件夹对象
     * @return 完整路径
     */
    @NonNull
    String getFilePath(@NonNull RawFileObject file);

    /**
     * 通过路径查找文件/文件夹
     *
     * @param path 路径切片（/test/123 -> {"test", "123"}）
     * @return 文件或文件夹对象
     */
    @Nullable
    FullRawFileObject resolveFile(@NonNull String[] path);
}
