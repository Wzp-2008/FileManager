package cn.wzpmc.filemanager.interfaces.impl;

import cn.wzpmc.filemanager.entities.files.FullRawFileObject;
import cn.wzpmc.filemanager.entities.files.enums.FileType;
import cn.wzpmc.filemanager.entities.vo.FolderVo;
import cn.wzpmc.filemanager.mapper.FolderMapper;
import cn.wzpmc.filemanager.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.List;

import static cn.wzpmc.filemanager.entities.vo.table.FolderVoTableDef.FOLDER_VO;

@Component
public class SimpleResolver extends SimplePathResolver {
    private FileService fileService;
    @Autowired
    public SimpleResolver(FolderMapper folderMapper) {
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
        return resolveFile(removeEmptyPath(path), -1);
    }

    private FullRawFileObject resolveFile(List<String> path, long parentId) {
        String currentLayerName = path.get(0);
        if (path.size() == 1) {
            int lastDotIndex = currentLayerName.lastIndexOf('.');
            String name = currentLayerName;
            String ext = "";
            if (lastDotIndex != -1) {
                name = currentLayerName.substring(0, lastDotIndex);
                ext = currentLayerName.substring(lastDotIndex + 1);
            }
            List<FullRawFileObject> files = this.fileService.getRawFilesByNameAndFolder(name, ext, parentId);
            int size = files.size();
            if (size == 0) {
                return null;
            }
            if (size == 1) {
                return files.get(0);
            }
            return files.stream().filter(e -> e.getType().equals(FileType.FILE)).findFirst().orElse(null);
        }
        FolderVo folderVo = folderMapper.selectOneByCondition(FOLDER_VO.NAME.eq(currentLayerName).and(FOLDER_VO.PARENT.eq(parentId)));
        if (folderVo == null) {
            return null;
        }
        return resolveFile(path.subList(1, path.size()), folderVo.getId());
    }
}
