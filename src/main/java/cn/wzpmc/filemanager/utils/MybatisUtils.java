package cn.wzpmc.filemanager.utils;

import lombok.SneakyThrows;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class MybatisUtils {
    @SneakyThrows
    public static boolean isPgSQL(PreparedStatement ps) {
        return isPgSQL(ps.getConnection());
    }
    @SneakyThrows
    public static boolean isPgSQL(Connection conn) {
        String databaseProductName = conn.getMetaData().getDatabaseProductName();
        return databaseProductName.equals("PostgreSQL");
    }
}
