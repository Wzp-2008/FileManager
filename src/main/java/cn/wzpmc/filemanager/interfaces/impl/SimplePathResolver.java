package cn.wzpmc.filemanager.interfaces.impl;

import cn.wzpmc.filemanager.entities.files.RawFileObject;
import cn.wzpmc.filemanager.entities.vo.FolderVo;
import cn.wzpmc.filemanager.interfaces.FilePathService;
import cn.wzpmc.filemanager.mapper.FolderMapper;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;

import java.util.Arrays;
import java.util.List;

/**
 * 简单路径查找器（只实现了 文件 -> 路径，需要手动实现路径 -> 文件）
 */
@RequiredArgsConstructor
public abstract class SimplePathResolver implements FilePathService {
    protected final FolderMapper folderMapper;

    @NonNull
    @Override
    public String getFilePath(@NonNull RawFileObject file) {
        return resolvePath(file.getParent()) + RawFileObject.getRawFileName(file);
    }

    /**
     * 从下往上遍历父文件夹，生成路径
     *
     * @param id 文件ID
     * @return 文件路径
     */
    private String resolvePath(long id) {
        if (id == -1) {
            return "/";
        }
        FolderVo folderVo = folderMapper.selectOneById(id);
        assert folderVo != null;
        long parent = folderVo.getParent();
        String name = folderVo.getName();
        return resolvePath(parent) + name + "/";
    }

    /**
     * 从路径字符串中移除空字符串（由于路径以/分隔，可能会出现空字符串）
     *
     * @param path 路径分割
     * @return 路径层级列表
     */
    protected List<String> removeEmptyPath(String[] path) {
        return Arrays.stream(path).filter(e -> !e.isEmpty()).toList();
    }
}
