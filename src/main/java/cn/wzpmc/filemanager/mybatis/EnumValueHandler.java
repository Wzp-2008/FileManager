package cn.wzpmc.filemanager.mybatis;

import cn.wzpmc.filemanager.annotation.PgEnumName;
import cn.wzpmc.filemanager.utils.MybatisUtils;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.postgresql.util.PGobject;

import java.lang.reflect.Type;
import java.sql.*;

/**
 * 通用enum类型到数据库可认的enum类型转换器
 *
 * @param <E> 对应的enum类
 */
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
        // PostgreSQL需要特殊处理
        if (MybatisUtils.isPgSQL(ps)) {
            // 新建一个PG对象
            PGobject obj = new PGobject();
            Type rawType = this.getRawType();
            // 获取当前类型所属的类
            if (rawType instanceof Class<?> clazz) {
                // 获取pg使用的pg enum name注解值（对应pg数据库中的类型名称）
                PgEnumName annotation = clazz.getAnnotation(PgEnumName.class);
                // 将其转为对应的pg类型并放入查询参数
                String value = annotation.value();
                obj.setType(value);
                obj.setValue(parameter.name());
                ps.setObject(i, obj, Types.OTHER);
                return;
            }
        }
        // 非PostgreSQL则直接使用字符串
        ps.setString(i, parameter.name());
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
