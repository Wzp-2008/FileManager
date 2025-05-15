package cn.wzpmc.filemanager.interfaces.impl;

import cn.wzpmc.filemanager.entities.files.FullRawFileObject;
import cn.wzpmc.filemanager.entities.files.RawFileObject;
import cn.wzpmc.filemanager.entities.vo.FolderVo;
import cn.wzpmc.filemanager.mapper.FolderMapper;
import cn.wzpmc.filemanager.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ReverseResolver extends SimplePathResolver {
    private FileService fileService;
    @Autowired
    public ReverseResolver(FolderMapper folderMapper) {
        super(folderMapper);
    }

    @Autowired
    @Lazy
    public void setFileService(FileService fileService) {
        this.fileService = fileService;
    }
    @Nullable
    @Override
    public FullRawFileObject resolveFile(@NonNull String[] path) {
        List<String> pathList = removeEmptyPath(path);
        String targetFileName = pathList.get(pathList.size() - 1);
        int lastDotIndex = targetFileName.lastIndexOf('.');
        String name = targetFileName;
        String ext = "";
        if (lastDotIndex != -1) {
            name = targetFileName.substring(0, lastDotIndex);
            ext = targetFileName.substring(lastDotIndex + 1);
        }
        List<FullRawFileObject> rawFileObjects = fileService.getRawFilesByName(name, ext);
        if (rawFileObjects.isEmpty()) return null;
        if (rawFileObjects.size() == 1) return rawFileObjects.get(0);
        List<Long> possibleParents = rawFileObjects.stream().map(RawFileObject::getParent).toList();
        Optional<FullRawFileObject> inRoot = rawFileObjects.stream().filter(e -> e.getParent() == -1).findFirst();
        if (inRoot.isPresent()) {
            if (pathList.size() <= 1) {
                return inRoot.get();
            }
        }
        List<FolderVo> folderVos = folderMapper.selectListByIds(possibleParents);
        FolderVo parent = reverseFindFileParent(folderVos, pathList.subList(0, pathList.size() - 1));
        if (parent == null) return null;
        Optional<FullRawFileObject> first = rawFileObjects.stream().filter(e -> e.getParent() == parent.getId()).findFirst();
        return first.orElse(null);
    }

    private FolderVo reverseFindFileParent(List<FolderVo> possibleParent, List<String> path) {
        if (path.isEmpty()) return null;
        if (possibleParent.size() == 1) return possibleParent.get(0);
        String currentLayerName = path.get(path.size() - 1);
        List<FolderVo> folderVoStream = possibleParent.stream().filter(e -> e.getName().equals(currentLayerName)).toList();
        Optional<FolderVo> inRoot = folderVoStream.stream().filter(e -> e.getParent() == -1).findFirst();
        if (inRoot.isPresent()) {
            if (path.size() <= 1) {
                return inRoot.get();
            }
        }
        List<Long> list = folderVoStream.stream().map(FolderVo::getParent).toList();
        if (list.isEmpty()) return null;
        List<FolderVo> parents = folderMapper.selectListByIds(list);
        FolderVo parent = reverseFindFileParent(parents, path.subList(0, path.size() - 1));
        if (parent == null) return null;
        Optional<FolderVo> first = folderVoStream.stream().filter(e -> e.getParent() == parent.getId()).findFirst();
        return first.orElse(null);
    }
}
