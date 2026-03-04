package cn.wzpmc.filemanager.mybatis;

import cn.wzpmc.filemanager.utils.MybatisUtils;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.postgresql.util.PGobject;

import java.lang.reflect.Type;
import java.sql.*;

public class EnumValueHandler<E extends Enum<E>> extends BaseTypeHandler<E> {
    private Class<E> getRawClass() {
        Type rawType = this.getRawType();
        if (rawType instanceof Class<?> clazz) {
            //noinspection unchecked
            return (Class<E>) clazz;
        }
        throw new RuntimeException("Cannot cast type " + rawType + "to class");
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, E parameter, JdbcType jdbcType) throws SQLException {
        if (MybatisUtils.isPgSQL(ps)) {
            PGobject obj = new PGobject();
            Type rawType = this.getRawType();
            if (rawType instanceof Class<?> clazz) {
                PgEnumName annotation = clazz.getAnnotation(PgEnumName.class);
                String value = annotation.value();
                obj.setType(value);
                obj.setValue(parameter.name());
                ps.setObject(i, obj, Types.OTHER);
                return;
            }
        }
        ps.setObject(i, parameter);
    }

    @Override
    public E getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String v = rs.getString(columnName);
        return v == null ? null : E.valueOf(this.getRawClass(), v);
    }

    @Override
    public E getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String v = rs.getString(columnIndex);
        return v == null ? null : E.valueOf(this.getRawClass(), v);
    }

    @Override
    public E getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String v = cs.getString(columnIndex);
        return v == null ? null : E.valueOf(this.getRawClass(), v);
    }
}
