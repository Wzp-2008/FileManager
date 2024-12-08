package cn.wzpmc.filemanager.utils;

import com.mybatisflex.core.query.FunctionQueryColumn;
import com.mybatisflex.core.query.QueryColumn;

import static com.mybatisflex.core.query.QueryMethods.string;

public class MyQueryMethods {
    public static QueryColumn jsonReadColumn(QueryColumn column, String express) {
        String[] dotedSplit = express.split("\\.");
        StringBuilder sb = new StringBuilder("$");
        for (String s : dotedSplit) {
            sb.append('.');
            sb.append('"');
            sb.append(s);
            sb.append('"');
        }
        return new FunctionQueryColumn("JSON_EXTRACT", column, string(sb.toString()));
    }
}
