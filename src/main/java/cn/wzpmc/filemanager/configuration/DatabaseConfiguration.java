package cn.wzpmc.filemanager.configuration;

import org.apache.ibatis.mapping.DatabaseIdProvider;
import org.apache.ibatis.mapping.VendorDatabaseIdProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

/**
 * 数据库配置
 */
@Configuration
public class DatabaseConfiguration {
    /**
     * 配置数据库族提供器，用于在不同的数据库上执行不同的sql
     *
     * @return 数据库ID提供器
     */
    @Bean
    public DatabaseIdProvider databaseIdProvider() {
        VendorDatabaseIdProvider vendorDatabaseIdProvider = new VendorDatabaseIdProvider();
        Properties properties = new Properties();
        // 将MySQL和MariaDB归为mysql族
        properties.setProperty("MySQL", "mysql");
        properties.setProperty("MariaDB", "mysql");
        // 将PostgreSQL归为postgres族
        properties.setProperty("PostgreSQL", "postgres");
        vendorDatabaseIdProvider.setProperties(properties);
        return vendorDatabaseIdProvider;
    }
}
