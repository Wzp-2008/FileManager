package cn.wzpmc.filemanager.events;

import cn.wzpmc.filemanager.config.FileManagerProperties;
import cn.wzpmc.filemanager.mapper.InitializationMapper;
import cn.wzpmc.filemanager.service.UserService;
import com.mybatisflex.core.audit.AuditManager;
import com.mybatisflex.core.audit.ConsoleMessageCollector;
import com.mybatisflex.core.audit.MessageCollector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 启动事件监听器
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class StartEventListener {
    private final InitializationMapper initializationMapper;
    private final FileManagerProperties properties;
    private final UserService userService;

    /**
     * 在启动时执行
     */
    @EventListener
    public void onStart(ApplicationStartedEvent ignored) {
        // 创建所有对应表（CREATE TABLE IF NOT EXISTS）
        initializationMapper.createFileTable();
        initializationMapper.createUserTable();
        initializationMapper.createStatisticsTable();
        initializationMapper.createFolderTable();
        initializationMapper.createPrefTable();
        initializationMapper.createFingerprintTable();
        initializationMapper.createChunksTable();
        initializationMapper.createChunkFileTable();
        //开启审计功能（输出运行的SQL）
        AuditManager.setAuditEnable(true);
        MessageCollector collector = new ConsoleMessageCollector();
        AuditManager.setMessageCollector(collector);
        // 调试模式日志
        if (properties.isDev()) {
            log.info("当前为开发环境，关闭日志收集，如需启用请删除wzp.filemanager.dev配置项！");
        }
        // 只读模式日志
        if (properties.isReadonly()) {
            log.info("当前为只读模式，不保存任何数据，也不允许上传任何数据，如需关闭请删除wzp.filemanager.readonly配置项");
            return;
        }
        // 检查是否需要生成第一个管理员密钥并尝试生成
        userService.tryGenFirstAdminKey();
    }
}
