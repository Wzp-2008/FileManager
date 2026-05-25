package cn.wzpmc.filemanager.interfaces.impl;

import cn.wzpmc.filemanager.entities.files.FullRawFileObject;
import cn.wzpmc.filemanager.entities.files.RawFileObject;
import cn.wzpmc.filemanager.entities.vo.FolderVo;
import cn.wzpmc.filemanager.mapper.FolderMapper;
import cn.wzpmc.filemanager.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * 反向路径查找器（路径 -> 文件）
 */
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
        // 移除路径分隔中空字符的部分
        List<String> pathList = removeEmptyPath(path);
        // 找要寻找的最后一层的文件/文件夹名
        String targetFileName = pathList.get(pathList.size() - 1);
        // 根据.分割文件名和扩展名
        int lastDotIndex = targetFileName.lastIndexOf('.');
        String name = targetFileName;
        String ext = "";
        if (lastDotIndex != -1) {
            name = targetFileName.substring(0, lastDotIndex);
            ext = targetFileName.substring(lastDotIndex + 1);
        }
        // 通过文件名和扩展名查找文件/文件夹
        List<FullRawFileObject> rawFileObjects = fileService.getRawFilesByName(name, ext);
        // 如果一个都没找到直接返回空
        if (rawFileObjects.isEmpty()) return null;
        // 如果只找到了一个就返回找到的那个
        if (rawFileObjects.size() == 1) return rawFileObjects.get(0);
        // 获取所有文件/文件夹的父级文件夹
        List<Long> possibleParents = rawFileObjects.stream().map(RawFileObject::getParent).toList();
        // 检查是否已经追查到父级了，如果已经在父级了且文件路径只有一层了，就直接返回
        Optional<FullRawFileObject> inRoot = rawFileObjects.stream().filter(e -> e.getParent() == -1).findFirst();
        if (inRoot.isPresent()) {
            if (pathList.size() <= 1) {
                return inRoot.get();
            }
        }
        // 获取可能的父级文件夹信息
        List<FolderVo> folderVos = folderMapper.selectListByIds(possibleParents);
        // 反向递归查找父级文件夹，直到找到路径的根部或者找不到为止
        FolderVo parent = reverseFindFileParent(folderVos, pathList.subList(0, pathList.size() - 1));
        // 找不到就直接返回
        if (parent == null) return null;
        // 过滤出当前文件夹下的对应文件
        Optional<FullRawFileObject> first = rawFileObjects.stream().filter(e -> e.getParent() == parent.getId()).findFirst();
        // 返回
        return first.orElse(null);
    }

    /**
     * 在父级文件夹中查找当前路径
     *
     * @param possibleParent 所有找到的父级文件夹
     * @param path           路径列表
     * @return 查找到的文件夹
     */
    private FolderVo reverseFindFileParent(List<FolderVo> possibleParent, List<String> path) {
        // 如果路径层已经空了那就直接返回空
        if (path.isEmpty()) return null;
        // 如果只找到了一个可能的父级文件夹则直接返回
        if (possibleParent.size() == 1) return possibleParent.get(0);
        // 当前层名称
        String currentLayerName = path.get(path.size() - 1);
        // 过滤出当前层名称的文件夹
        List<FolderVo> folderVoStream = possibleParent.stream().filter(e -> e.getName().equals(currentLayerName)).toList();
        // 如果当前路径只剩一层且有文件在根文件夹下则直接返回
        Optional<FolderVo> inRoot = folderVoStream.stream().filter(e -> e.getParent() == -1).findFirst();
        if (inRoot.isPresent()) {
            if (path.size() <= 1) {
                return inRoot.get();
            }
        }
        // 获取当前层文件夹的父级文件夹
        List<Long> list = folderVoStream.stream().map(FolderVo::getParent).toList();
        // 如果没有找到的父级文件夹则直接返回空
        if (list.isEmpty()) return null;
        // 获取父级文件夹信息
        List<FolderVo> parents = folderMapper.selectListByIds(list);
        // 递归调用
        FolderVo parent = reverseFindFileParent(parents, path.subList(0, path.size() - 1));
        // 如果parent找不到就返回空
        if (parent == null) return null;
        // 从所有找到的父级中过滤出当前层ID的文件夹
        Optional<FolderVo> first = folderVoStream.stream().filter(e -> e.getParent() == parent.getId()).findFirst();
        // 返回
        return first.orElse(null);
    }
}
