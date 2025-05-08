package cn.wzpmc.filemanager.events;

import cn.wzpmc.filemanager.config.FileManagerProperties;
import cn.wzpmc.filemanager.mapper.InitializationMapper;
import com.mybatisflex.core.audit.AuditManager;
import com.mybatisflex.core.audit.ConsoleMessageCollector;
import com.mybatisflex.core.audit.MessageCollector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class StartEventListener {
    private final InitializationMapper initializationMapper;
    private final FileManagerProperties properties;

    @EventListener
    public void onStart(ApplicationStartedEvent ignored) {
        initializationMapper.createUserTable();
        initializationMapper.createStatisticsTable();
        initializationMapper.createFolderTable();
        initializationMapper.createFileTable();
        initializationMapper.createPrefTable();
        initializationMapper.createRawFileView();
        //开启审计功能
        AuditManager.setAuditEnable(true);
        MessageCollector collector = new ConsoleMessageCollector();
        AuditManager.setMessageCollector(collector);
        if (properties.isDev()) {
            log.info("当前为开发环境，关闭日志收集，如需启用请删除wzp.filemanager.dev配置项！");
        }
    }
}
