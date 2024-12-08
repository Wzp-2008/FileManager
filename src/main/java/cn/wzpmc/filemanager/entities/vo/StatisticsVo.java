package cn.wzpmc.filemanager.entities.vo;

import cn.wzpmc.filemanager.entities.statistics.enums.Actions;
import com.alibaba.fastjson2.annotation.JSONCompiled;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

import java.util.Date;

@Table("statistics")
@Data
@NoArgsConstructor
@JSONCompiled
public class StatisticsVo {
    /***
     * 操作者（可能为空）
     */
    @Nullable
    private Long actor;
    /**
     * 具体操作
     */
    private Actions action;
    /**
     * 操作参数（一般为JSON字符串）
     */
    private String params;
    /**
     * 操作时间
     */
    @Column(onInsertValue = "now()")
    private Date time;
    /**
     * 若为下载操作时的下载文件ID
     * @ignore
     */
    @Column(value = "download_file_id", ignore = true)
    private Integer downloadFileId;

    public StatisticsVo(@Nullable Long actor, Actions action, String params) {
        this.actor = actor;
        this.action = action;
        this.params = params;
    }
}