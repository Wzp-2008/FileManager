package cn.wzpmc.filemanager;

import com.mybatisflex.core.FlexGlobalConfig;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@ConfigurationPropertiesScan(basePackages = {"cn.wzpmc.filemanager.config"})
@MapperScan("cn.wzpmc.filemanager.mapper")
@EnableTransactionManagement
@EnableAsync
public class FileManagerApplication {

    public static void main(String[] args) {
        FlexGlobalConfig globalConfig = FlexGlobalConfig.getDefaultConfig();
        globalConfig.setNormalValueOfLogicDelete(false);
        globalConfig.setDeletedValueOfLogicDelete(true);
        SpringApplication.run(FileManagerApplication.class, args);
    }

}