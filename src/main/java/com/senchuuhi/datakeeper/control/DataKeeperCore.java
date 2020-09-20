package com.senchuuhi.datakeeper.control;

import com.alibaba.fastjson.JSONObject;
import com.senchuuhi.datakeeper.exception.CheckInitDataErrorException;
import com.senchuuhi.datakeeper.utils.FileUtils;
import com.senchuuhi.datakeeper.utils.SqlLiteUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * 数据核心
 * Created by QQQZF on 2020/9/20.
 */
public class DataKeeperCore extends DataKeeperConstant {

    // 日志
    private static Logger logger = LoggerFactory.getLogger(DataKeeperCore.class);


    static {
        BASE_DB_FILE_PATH = FileUtils.getBasePath() + DB_FILE_NAME + "_" + VERSION + ".db";
    }

    public static void init() {
        Connection conn = SqlLiteUtils.getConnection(BASE_DB_FILE_PATH);
        if (!checkTable(conn)) {
            logger.info("Can't find table data, try init table now...");
            initTable(conn);
        }
    }

    public static void main(String[] args) {
        init();
    }


    /**
     * 检查表是否存在
     * @return
     */
    public static boolean checkTable(Connection conn) {
        try {
            int step = 0;
            List<JSONObject> list = SqlLiteUtils.queryData(conn, "select name from sqlite_master where type = 'table'");
            if (list != null && list.size() > 0) {
                for (JSONObject object : list) {
                    if (object.getString("name").equals(LIST_TABLE_NAME)) {
                        step += 1;
                    } else if (object.getString("name").equals(MAP_TABLE_NAME)) {
                        step += 2;
                    } else if (object.getString("name").equals(SET_TABLE_NAME)) {
                        step += 4;
                    }
                }
            }
            if (step == 7) {
                return true;
            } else if (step > 0) {
                throw new CheckInitDataErrorException("CheckTable fail, can't find all tables[{}, {}, {}]. \r\nPlease check your database!\r\nDB File Path: {}", LIST_TABLE_NAME, MAP_TABLE_NAME, SET_TABLE_NAME, BASE_DB_FILE_PATH);
            }
        } catch (SQLException e) {
            throw new CheckInitDataErrorException(e, "CheckTable fail:{}", e.getMessage());
        }
        return false;
    }


    public static void initTable(Connection conn) {
        logger.info("Init Table, DB file path:{}", BASE_DB_FILE_PATH);
    }


}
