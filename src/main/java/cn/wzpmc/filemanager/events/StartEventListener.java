package cn.wzpmc.filemanager.events;

import cn.wzpmc.filemanager.mapper.InitializationMapper;
import com.mybatisflex.core.audit.AuditManager;
import com.mybatisflex.core.audit.ConsoleMessageCollector;
import com.mybatisflex.core.audit.MessageCollector;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StartEventListener {
    private final InitializationMapper initializationMapper;

    @EventListener
    public void onStart(ApplicationStartedEvent ignored) {
        initializationMapper.createUserTable();
        initializationMapper.createStatisticsTable();
        initializationMapper.createFolderTable();
        initializationMapper.createFileTable();
        initializationMapper.createRawFileView();
        //开启审计功能
        AuditManager.setAuditEnable(true);
        MessageCollector collector = new ConsoleMessageCollector();
        AuditManager.setMessageCollector(collector);
    }
}
