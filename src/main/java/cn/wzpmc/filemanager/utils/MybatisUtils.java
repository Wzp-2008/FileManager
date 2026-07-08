package cn.wzpmc.filemanager.utils;

import lombok.SneakyThrows;

import java.sql.Connection;
import java.sql.PreparedStatement;

/**
 * MyBatis工具类
 */
public class MybatisUtils {
    /**
     * 使用语句判断是否为pgsql数据库
     *
     * @param ps 预编译语句
     * @return 是否为pgsql数据库
     * @see MybatisUtils#isPgSQL(Connection)
     */
    @SneakyThrows
    public static boolean isPgSQL(PreparedStatement ps) {
        return isPgSQL(ps.getConnection());
    }

    /**
     * 通过数据库连接的ProductName判断是否为pgsql数据库
     *
     * @param conn 数据库连接
     * @return 是否为pgsql数据库
     */
    @SneakyThrows
    public static boolean isPgSQL(Connection conn) {
        String databaseProductName = conn.getMetaData().getDatabaseProductName();
        return databaseProductName.equals("PostgreSQL");
    }
}
