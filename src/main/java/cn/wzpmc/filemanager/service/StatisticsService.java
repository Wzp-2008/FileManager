package cn.wzpmc.filemanager.service;

import cn.wzpmc.filemanager.config.FileManagerProperties;
import cn.wzpmc.filemanager.entities.statistics.enums.Actions;
import cn.wzpmc.filemanager.entities.vo.StatisticsVo;
import cn.wzpmc.filemanager.entities.vo.UserVo;
import cn.wzpmc.filemanager.mapper.StatisticsMapper;
import com.alibaba.fastjson2.JSONObject;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StatisticsService {
    private final StatisticsMapper statisticsMapper;
    private final FileManagerProperties properties;

    public void insertAction(@Nullable UserVo actor, Actions actions, @Nullable JSONObject params) {
        if (properties.isDev()) return;
        if (properties.isReadonly()) throw new RuntimeException("只读模式，无法写入数据！");
        statisticsMapper.insert(new StatisticsVo(actor != null ? actor.getId() : null, actions, params));
    }

    public void insertAction(@Nullable UserVo actor, Actions actions) {
        this.insertAction(actor, actions, null);
    }

    public void insertAction(Actions actions, @Nullable JSONObject params) {
        this.insertAction(null, actions, params);
    }

    public void insertAction(Actions actions) {
        this.insertAction(actions, null);
    }
}
