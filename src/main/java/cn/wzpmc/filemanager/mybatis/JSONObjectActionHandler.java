package cn.wzpmc.filemanager.mybatis;

import cn.wzpmc.filemanager.utils.MybatisUtils;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.mybatisflex.core.handler.BaseJsonTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.postgresql.util.PGobject;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

public class JSONObjectActionHandler extends BaseJsonTypeHandler<JSONObject> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, JSONObject parameter, JdbcType jdbcType) throws SQLException {
        if (MybatisUtils.isPgSQL(ps)) {
            PGobject obj = new PGobject();
            obj.setType("jsonb");
            obj.setValue(this.toJson(parameter));
            ps.setObject(i, obj, Types.OTHER);
            return;
        }
        super.setNonNullParameter(ps, i, parameter, jdbcType);
    }

    @Override
    protected JSONObject parseJson(String json) {
        return JSON.parseObject(json);
    }

    @Override
    protected String toJson(JSONObject object) {
        return object.toString();
    }
}
