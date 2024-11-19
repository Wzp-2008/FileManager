package cn.wzpmc.filemanager.service;

import cn.wzpmc.filemanager.entities.statistics.enums.Actions;
import cn.wzpmc.filemanager.entities.vo.StatisticsVo;
import cn.wzpmc.filemanager.entities.vo.UserVo;
import cn.wzpmc.filemanager.mapper.StatisticsMapper;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StatisticsService {
    private final StatisticsMapper statisticsMapper;

    public void insertAction(@Nullable UserVo actor, Actions actions, @Nullable Object params) {
        statisticsMapper.insert(new StatisticsVo(actor != null ? actor.getId() : null, actions, params != null ? params.toString() : null));
    }

    public void insertAction(@Nullable UserVo actor, Actions actions) {
        this.insertAction(actor, actions, null);
    }

    public void insertAction(Actions actions, @Nullable Object params) {
        this.insertAction(null, actions, params);
    }

    public void insertAction(Actions actions) {
        this.insertAction(actions, null);
    }
}
