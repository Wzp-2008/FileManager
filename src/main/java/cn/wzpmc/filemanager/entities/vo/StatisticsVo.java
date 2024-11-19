package cn.wzpmc.filemanager.entities.vo;

import cn.wzpmc.filemanager.entities.statistics.enums.Actions;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Table("statistics")
@Data
@NoArgsConstructor
public class StatisticsVo {
    private Long actor;
    private Actions action;
    private String params;
    @Column(onInsertValue = "now()")
    private Date time;

    public StatisticsVo(Long actor, Actions action, String params) {
        this.actor = actor;
        this.action = action;
        this.params = params;
    }
}