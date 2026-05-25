package cn.wzpmc.filemanager.interfaces.impl;

import cn.wzpmc.filemanager.entities.files.FullRawFileObject;
import cn.wzpmc.filemanager.mapper.FolderMapper;
import cn.wzpmc.filemanager.service.FileService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;

/**
 * 复合路径解析器（使用简单路径解析器实现文件->路径的解析，若同名文件数量比路径层级量少时使用简单解析（通过路径层级解析），否则使用反向查找解析器（通过文件名寻找对应路径））
 *
 * @see ReverseResolver
 * @see SimpleResolver
 * @see SimplePathResolver
 */
@Component
@Primary
@Log4j2
public class ComplexResolver extends SimplePathResolver {
    private final ReverseResolver reverseResolver;
    private final SimpleResolver simpleResolver;
    private FileService fileService;

    @Autowired
    public ComplexResolver(FolderMapper folderMapper, ReverseResolver reverseResolver, SimpleResolver simpleResolver) {
        super(folderMapper);
        this.reverseResolver = reverseResolver;
        this.simpleResolver = simpleResolver;
    }

    @Autowired
    @Lazy
    public void setFileService(FileService fileService) {
        this.fileService = fileService;
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
        // 启动计时
        long start = new Date().getTime();
        // 根据最后一层路径的文件名和扩展名获取该名称文件数量
        long totalRawFileCount = this.fileService.getRawFilesCountByName(name, ext);
        // 如果找不到对应文件则直接返回空
        if (totalRawFileCount == 0) return null;
        // 若文件数量比路径层数多则使用简单解析
        if (totalRawFileCount > path.length) {
            log.info("use simple resolver to solve path with {}", strPath);
            FullRawFileObject rawFileObject = simpleResolver.resolveFile(path);
            long end = new Date().getTime();
            // 结束计时并输出日志
            log.info("solve path {} cost {}ms", strPath, end - start);
            return rawFileObject;
        }
        // 否则使用反向解析
        log.info("use reverse resolver to solve path with {}", strPath);
        FullRawFileObject rawFileObject = reverseResolver.resolveFile(path);
        long end = new Date().getTime();
        // 结束计时并输出日志
        log.info("solve path {} cost {}ms", strPath, end - start);
        return rawFileObject;
    }
}
