package cn.wzpmc.filemanager.entities.files.enums;

import com.mybatisflex.core.query.QueryColumn;
import lombok.RequiredArgsConstructor;

import static com.mybatisflex.core.query.QueryMethods.column;

@RequiredArgsConstructor
public enum SortField {
    /**
     * 通过ID排序（默认）
     */
    ID(column("id")),
    /**
     * 通过文件夹排序
     */
    NAME(column("name")),
    /**
     * 通过文件扩展名排序
     */
    EXT(column("ext")),
    /**
     * 通过文件上传时间排序
     */
    TIME(column("time")),
    /**
     * 通过文件上传者排序
     */
    UPLOADER(column("owner")),
    /**
     * 通过文件下载次数排序
     */
    DOWNLOAD_COUNT(column("down_count")),
    /**
     * 根据文件大小排序
     */
    SIZE(column("size"));
    public final QueryColumn column;
}
