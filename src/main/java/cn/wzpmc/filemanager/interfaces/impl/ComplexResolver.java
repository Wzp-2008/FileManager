package cn.wzpmc.filemanager.interfaces.impl;

import cn.wzpmc.filemanager.entities.files.FullRawFileObject;
import cn.wzpmc.filemanager.mapper.FileMapper;
import cn.wzpmc.filemanager.mapper.FolderMapper;
import cn.wzpmc.filemanager.mapper.RawFileMapper;
import com.mybatisflex.core.query.QueryCondition;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;

import static cn.wzpmc.filemanager.entities.files.table.FullRawFileObjectTableDef.FULL_RAW_FILE_OBJECT;

@Component
@Primary
@Log4j2
public class ComplexResolver extends SimplePathResolver {
    private final ReverseResolver reverseResolver;
    private final SimpleResolver simpleResolver;
    private final RawFileMapper rawFileMapper;
    @Autowired
    public ComplexResolver(FileMapper fileMapper, FolderMapper folderMapper, ReverseResolver reverseResolver, SimpleResolver simpleResolver, RawFileMapper rawFileMapper) {
        super(fileMapper, folderMapper);
        this.reverseResolver = reverseResolver;
        this.simpleResolver = simpleResolver;
        this.rawFileMapper = rawFileMapper;
    }

    @Override
    @Nullable
    public FullRawFileObject resolveFile(@NonNull String[] path) {
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
        QueryCondition extCondition = FULL_RAW_FILE_OBJECT.EXT.eq(ext);
        if (ext.isEmpty()) {
            extCondition = extCondition.or(FULL_RAW_FILE_OBJECT.EXT.isNull());
        }
        long totalRawFileCount = this.rawFileMapper.selectCountByCondition(FULL_RAW_FILE_OBJECT.NAME.eq(name).and(extCondition));
        if (totalRawFileCount == 0)  return null;
        if (totalRawFileCount > path.length) {
            log.info("use simple resolver to solve path with {}", strPath);
            FullRawFileObject rawFileObject = simpleResolver.resolveFile(path);
            long end = new Date().getTime();
            log.info("solve path {} cost {}ms", strPath, end - start);
            return rawFileObject;
        }
        log.info("use reverse resolver to solve path with {}", strPath);
        FullRawFileObject rawFileObject = reverseResolver.resolveFile(path);
        long end = new Date().getTime();
        log.info("solve path {} cost {}ms", strPath, end - start);
        return rawFileObject;
    }
}
