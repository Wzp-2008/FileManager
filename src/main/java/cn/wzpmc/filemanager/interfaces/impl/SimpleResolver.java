package cn.wzpmc.filemanager.interfaces.impl;

import cn.wzpmc.filemanager.entities.files.RawFileObject;
import cn.wzpmc.filemanager.entities.vo.FileVo;
import cn.wzpmc.filemanager.entities.vo.FolderVo;
import cn.wzpmc.filemanager.mapper.FileMapper;
import cn.wzpmc.filemanager.mapper.FolderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.List;

import static cn.wzpmc.filemanager.entities.vo.table.FileVoTableDef.FILE_VO;
import static cn.wzpmc.filemanager.entities.vo.table.FolderVoTableDef.FOLDER_VO;

@Component
public class SimpleResolver extends SimplePathResolver {
    @Autowired
    public SimpleResolver(FileMapper fileMapper, FolderMapper folderMapper) {
        super(fileMapper, folderMapper);
    }

    @Nullable
    @Override
    public RawFileObject resolveFile(@NonNull String[] path) {
        return resolveFile(removeEmptyPath(path), -1);
    }

    private RawFileObject resolveFile(List<String> path, long parentId) {
        String currentLayerName = path.get(0);
        if (path.size() == 1) {
            int lastDotIndex = currentLayerName.lastIndexOf('.');
            String name = currentLayerName;
            String ext = "";
            if (lastDotIndex != -1) {
                name = currentLayerName.substring(0, lastDotIndex);
                ext = currentLayerName.substring(lastDotIndex + 1);
            }
            FileVo file = fileMapper.selectOneByCondition(FILE_VO.NAME.eq(name).and(FILE_VO.EXT.eq(ext)).and(FILE_VO.FOLDER.eq(parentId)));
            if (file != null) {
                return RawFileObject.of(file);
            }
            return RawFileObject.of(folderMapper.selectOneById(FOLDER_VO.NAME.eq(name).and(FOLDER_VO.PARENT.eq(parentId))));
        }
        FolderVo folderVo = folderMapper.selectOneByCondition(FOLDER_VO.NAME.eq(currentLayerName).and(FOLDER_VO.PARENT.eq(parentId)));
        if (folderVo == null) {
            return null;
        }
        return resolveFile(path.subList(1, path.size()), folderVo.getId());
    }
}
