package cn.wzpmc.filemanager.utils;

import lombok.SneakyThrows;

import java.sql.PreparedStatement;

public class MybatisUtils {
    @SneakyThrows
    public static boolean isPgSQL(PreparedStatement ps) {
        String databaseProductName = ps.getConnection().getMetaData().getDatabaseProductName();
        return databaseProductName.equals("PostgreSQL");
    }
}
