package cn.wzpmc.filemanager;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.shell.command.annotation.EnableCommand;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@ConfigurationPropertiesScan(basePackages = {"cn.wzpmc.filemanager.config"})
@MapperScan("cn.wzpmc.filemanager.mapper")
@EnableTransactionManagement
@EnableCommand
@EnableAsync
public class FileManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(FileManagerApplication.class, args);
    }

}