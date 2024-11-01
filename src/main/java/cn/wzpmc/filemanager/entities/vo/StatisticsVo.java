package cn.wzpmc.filemanager.entities.vo;

import cn.wzpmc.filemanager.entities.statistics.enums.Actions;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.util.Date;

@Table("statistics")
@Data
public class StatisticsVo {
    private int actor;
    private Actions action;
    private String params;
    @Column(onInsertValue = "now()")
    private Date time;

}