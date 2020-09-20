package io.github.qianzf.datakeeper.utils;

import com.alibaba.fastjson.JSONObject;
import io.github.qianzf.datakeeper.exception.ConnectionErrorException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 底层保存数据的基础类型
 * Created by QQQZF on 2020/9/20.
 */
public class SqlLiteUtils {

    // 数据
    private static final String CLASS_NAME = "org.sqlite.JDBC";

    private static Connection connection;

    /**
     * 创建Sqlite数据库连接
     *
     */
    public static synchronized Connection getConnection(String dbFilePath) {
        if (connection == null) {
            try {
                Class.forName(CLASS_NAME);
                connection = DriverManager.getConnection("jdbc:sqlite:" + dbFilePath);
            } catch (Exception e) {
                throw new ConnectionErrorException(e, "Connection SqlLite error:{}", e.getMessage());
            }

        }
        return connection;
    }

    /**
     * 数据查询
     * @return
     */
    public static List<JSONObject> queryData(Connection conn, String sql) throws SQLException {
        Statement statement = conn.createStatement();
        ResultSet rs = statement.executeQuery(sql);
        String[] columns = null;
        List<JSONObject> result = new ArrayList<>();
        if (rs != null) {
            while (rs.next()) {
                if (columns == null) {
                    ResultSetMetaData md = rs.getMetaData();
                    int nums = md.getColumnCount();
                    columns = new String[nums];
                    for (int i = 0; i < nums; ++i) {
                        columns[i] = md.getColumnLabel(i + 1);
                    }
                }
                JSONObject e = new JSONObject();
                for (String columnLabel : columns) {
                    e.put(columnLabel, rs.getObject(columnLabel));
                }
                result.add(e);
            }
        }
        return result;
    }



}
