package cn.wzpmc.filemanager.interfaces.impl;

import cn.wzpmc.filemanager.entities.files.FullRawFileObject;
import cn.wzpmc.filemanager.entities.files.RawFileObject;
import cn.wzpmc.filemanager.entities.vo.FolderVo;
import cn.wzpmc.filemanager.mapper.FileMapper;
import cn.wzpmc.filemanager.mapper.FolderMapper;
import cn.wzpmc.filemanager.mapper.RawFileMapper;
import com.mybatisflex.core.query.QueryCondition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static cn.wzpmc.filemanager.entities.files.table.FullRawFileObjectTableDef.FULL_RAW_FILE_OBJECT;

@Component
public class ReverseResolver extends SimplePathResolver {
    private final RawFileMapper rawFileMapper;
    @Autowired
    public ReverseResolver(FileMapper fileMapper, FolderMapper folderMapper, RawFileMapper rawFileMapper) {
        super(fileMapper, folderMapper);
        this.rawFileMapper = rawFileMapper;
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
        QueryCondition extCondition = FULL_RAW_FILE_OBJECT.EXT.eq(ext);
        if (ext.isEmpty()) {
            extCondition = extCondition.or(FULL_RAW_FILE_OBJECT.EXT.isNull());
        }
        List<FullRawFileObject> rawFileObjects = this.rawFileMapper.selectListByCondition(FULL_RAW_FILE_OBJECT.NAME.eq(name).and(extCondition));
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
