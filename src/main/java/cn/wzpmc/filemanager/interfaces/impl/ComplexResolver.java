package cn.wzpmc.filemanager.interfaces.impl;

import cn.wzpmc.filemanager.entities.files.RawFileObject;
import cn.wzpmc.filemanager.mapper.FileMapper;
import cn.wzpmc.filemanager.mapper.FolderMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;

import static cn.wzpmc.filemanager.entities.vo.table.FileVoTableDef.FILE_VO;
import static cn.wzpmc.filemanager.entities.vo.table.FolderVoTableDef.FOLDER_VO;

@Component
@Primary
@Log4j2
public class ComplexResolver extends SimplePathResolver {
    private final ReverseResolver reverseResolver;
    private final SimpleResolver simpleResolver;
    @Autowired
    public ComplexResolver(FileMapper fileMapper, FolderMapper folderMapper, ReverseResolver reverseResolver, SimpleResolver simpleResolver) {
        super(fileMapper, folderMapper);
        this.reverseResolver = reverseResolver;
        this.simpleResolver = simpleResolver;
    }

    @Override
    @Nullable
    public RawFileObject resolveFile(@NonNull String[] path) {
        String strPath = Arrays.toString(path);
        String targetFileName = path[path.length - 1];
        int lastDotIndex = targetFileName.lastIndexOf('.');
        String name = targetFileName;
        String ext = "";
        if (lastDotIndex != -1) {
            name = targetFileName.substring(0, lastDotIndex);
            ext = targetFileName.substring(lastDotIndex + 1);
        }
        long start = new Date().getTime();
        long totalRawFileCount = this.fileMapper.selectCountByCondition(FILE_VO.NAME.eq(name).and(FILE_VO.EXT.eq(ext))) + this.folderMapper.selectCountByCondition(FOLDER_VO.NAME.eq(name));
        if (totalRawFileCount == 0)  return null;
        if (totalRawFileCount > path.length) {
            log.info("use simple resolver to solve path with {}", strPath);
            RawFileObject rawFileObject = simpleResolver.resolveFile(path);
            long end = new Date().getTime();
            log.info("solve path {} cost {}ms", strPath, end - start);
            return rawFileObject;
        }
        log.info("use reverse resolver to solve path with {}", strPath);
        RawFileObject rawFileObject = reverseResolver.resolveFile(path);
        long end = new Date().getTime();
        log.info("solve path {} cost {}ms", strPath, end - start);
        return rawFileObject;
    }
}
