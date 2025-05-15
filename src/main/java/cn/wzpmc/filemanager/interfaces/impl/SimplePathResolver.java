package cn.wzpmc.filemanager.interfaces.impl;

import cn.wzpmc.filemanager.entities.files.RawFileObject;
import cn.wzpmc.filemanager.entities.vo.FolderVo;
import cn.wzpmc.filemanager.interfaces.FilePathService;
import cn.wzpmc.filemanager.mapper.FolderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;

import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
public abstract class SimplePathResolver implements FilePathService {
    protected final FolderMapper folderMapper;

    @NonNull
    @Override
    public String getFilePath(@NonNull RawFileObject file) {
        return resolvePath(file.getParent()) + RawFileObject.getRawFileName(file);
    }

    private String resolvePath(long id) {
        if (id == -1) {
            return "/";
        }
        FolderVo folderVo = folderMapper.selectOneById(id);
        long parent = folderVo.getParent();
        String name = folderVo.getName();
        return resolvePath(parent) + name + "/";
    }

    protected List<String> removeEmptyPath(String[] path) {
        return Arrays.stream(path).filter(e -> !e.isEmpty()).toList();
    }
}
