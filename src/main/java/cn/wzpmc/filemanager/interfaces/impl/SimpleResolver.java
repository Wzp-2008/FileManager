package cn.wzpmc.filemanager.interfaces.impl;

import cn.wzpmc.filemanager.entities.files.FullRawFileObject;
import cn.wzpmc.filemanager.entities.files.enums.FileType;
import cn.wzpmc.filemanager.entities.vo.FolderVo;
import cn.wzpmc.filemanager.mapper.FolderMapper;
import cn.wzpmc.filemanager.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.jspecify.annotations.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.List;

import static cn.wzpmc.filemanager.entities.vo.table.FolderVoTableDef.FOLDER_VO;

/**
 * 简单路径查找器（路径 -> 文件/文件夹）<br/>
 * 简单的通过遍历每层路径获取对应文件或文件夹ID
 */
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

    /**
     * 通过遍历路径解析文件
     *
     * @param path     路径切片
     * @param parentId 父文件夹ID
     * @return 文件/文件夹对象
     */
    private FullRawFileObject resolveFile(List<String> path, long parentId) {
        // 获取当前层级文件夹名称
        String currentLayerName = path.get(0);
        // 最后一层
        if (path.size() == 1) {
            int lastDotIndex = currentLayerName.lastIndexOf('.');
            String name = currentLayerName;
            String ext = "";
            // 若找到扩展名的点则将扩展名和文件名分割
            if (lastDotIndex != -1) {
                name = currentLayerName.substring(0, lastDotIndex);
                ext = currentLayerName.substring(lastDotIndex + 1);
            }
            // 查找文件或文件夹
            List<FullRawFileObject> files = this.fileService.getRawFilesByNameAndFolder(name, ext, parentId);
            int size = files.size();
            // 若没有返回则返回null
            if (size == 0) {
                return null;
            }
            // 若只找到了一个则返回找到的那一个
            if (size == 1) {
                return files.get(0);
            }
            // 若有同名文件/文件夹，则优先选择文件
            return files.stream().filter(e -> e.getType().equals(FileType.FILE)).findFirst().orElse(null);
        }
        // 否则查找当前层所在的文件夹并将其通过parentId递归传给下一层
        FolderVo folderVo = folderMapper.selectOneByCondition(FOLDER_VO.NAME.eq(currentLayerName).and(FOLDER_VO.PARENT.eq(parentId)));
        if (folderVo == null) {
            return null;
        }
        return resolveFile(path.subList(1, path.size()), folderVo.getId());
    }
}
