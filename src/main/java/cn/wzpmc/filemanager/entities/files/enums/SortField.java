package cn.wzpmc.filemanager.entities.files.enums;

import com.mybatisflex.core.query.QueryColumn;
import lombok.RequiredArgsConstructor;

import static cn.wzpmc.filemanager.entities.files.table.FullRawFileObjectTableDef.FULL_RAW_FILE_OBJECT;

@RequiredArgsConstructor
public enum SortField {
    /**
     * 通过ID排序（默认）
     */
    ID(FULL_RAW_FILE_OBJECT.ID),
    /**
     * 通过文件夹排序
     */
    NAME(FULL_RAW_FILE_OBJECT.NAME),
    /**
     * 通过文件扩展名排序
     */
    EXT(FULL_RAW_FILE_OBJECT.EXT),
    /**
     * 通过文件上传时间排序
     */
    TIME(FULL_RAW_FILE_OBJECT.TIME),
    /**
     * 通过文件上传者排序
     */
    UPLOADER(FULL_RAW_FILE_OBJECT.OWNER),
    /**
     * 通过文件下载次数排序
     */
    DOWNLOAD_COUNT(FULL_RAW_FILE_OBJECT.DOWN_COUNT);
    public final QueryColumn column;
}
