package cn.wzpmc.filemanager.entities.vo.table.custom;

import com.mybatisflex.core.query.QueryColumn;

/**
 * 扩展的统计表定义
 */
public class StatisticsVoTableDef extends cn.wzpmc.filemanager.entities.vo.table.StatisticsVoTableDef {
    public static final StatisticsVoTableDef STATISTICS_VO_EXT = new StatisticsVoTableDef();
    /**
     * 下载文件ID列（虚拟列）
     */
    public final QueryColumn DOWNLOAD_FILE_ID = new QueryColumn(this, "download_file_id");
}
