package cn.wzpmc.filemanager.entities.vo.table.custom;

import com.mybatisflex.core.query.QueryColumn;

public class StatisticsVoTableDef extends cn.wzpmc.filemanager.entities.vo.table.StatisticsVoTableDef {
    public static final StatisticsVoTableDef STATISTICS_VO_EXT = new StatisticsVoTableDef();
    public final QueryColumn DOWNLOAD_FILE_ID = new QueryColumn(this, "download_file_id");
}
