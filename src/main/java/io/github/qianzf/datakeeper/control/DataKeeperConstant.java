package io.github.qianzf.datakeeper.control;

/**
 * 常量数据
 * Created by QQQZF on 2020/9/20.
 */
public class DataKeeperConstant {

    // 基础文件路径
    protected static String BASE_DB_FILE_PATH = "";

    // 保存的数据文件名
    protected static String DB_FILE_NAME = "DataKeeper";

    // 当前数据库版本
    protected static String VERSION = "v_1_0";

    // 数据库表名
    protected static String LIST_TABLE_NAME = "DataKeeperList";

    // 建表语句
    protected static String LIST_TABLE_CREATE_SQL = "DROP TABLE DataKeeperList; CREATE TABLE \"DataKeeperList\" (\n" +
            "  \"key\" TEXT,\n" +
            "  \"value\" TEXT,\n" +
            "  \"score\" integer,\n" +
            "  \"expireTime\" integer\n" +
            ");\n" +
            "\n" +
            "CREATE INDEX \"INDEX_LIST_KEY\"\n" +
            "ON \"DataKeeperList\" (\n" +
            "  \"key\" ASC\n" +
            ");\n" +
            "\n" +
            "CREATE INDEX \"INDEX_LIST_TIME\"\n" +
            "ON \"DataKeeperList\" (\n" +
            "  \"expireTime\"\n" +
            ");\n" +
            "\n" +
            "CREATE TRIGGER \"ExpireListTrigger\"\n" +
            "BEFORE INSERT\n" +
            "ON \"DataKeeperList\"\n" +
            "BEGIN\n" +
            "\tDelete  from DataKeeperList where expireTime is not null and expireTime < strftime('%s','now');\n" +
            "END;";

    // MAP类型
    protected static String MAP_TABLE_NAME = "DataKeeperMap";

    protected static String MAP_TABLE_CREATE_SQL = "" +
            "DROP TABLE DataKeeperMap; CREATE TABLE \"DataKeeperMap\" (\n" +
            "  \"key\" text NOT NULL,\n" +
            "  \"value\" TEXT,\n" +
            "  \"score\" integer,\n" +
            "  \"expireTime\" integer,\n" +
            "  CONSTRAINT \"UQ_SET\" UNIQUE (\"key\" ASC) ON CONFLICT REPLACE\n" +
            ");\n" +
            "\n" +
            "CREATE INDEX \"INDEX_MAP_KEY\"\n" +
            "ON \"DataKeeperMap\" (\n" +
            "  \"key\" COLLATE BINARY ASC\n" +
            ");\n" +
            "\n" +
            "CREATE INDEX \"INDEX_MAP_TIME\"\n" +
            "ON \"DataKeeperMap\" (\n" +
            "  \"expireTime\"\n" +
            ");\n" +
            "\n" +
            "CREATE TRIGGER \"ExpireMapTrigger\"\n" +
            "BEFORE INSERT\n" +
            "ON \"DataKeeperMap\"\n" +
            "BEGIN\n" +
            "\tDelete  from DataKeeperMap where expireTime is not null and expireTime < strftime('%s','now');\n" +
            "END;";

    // SET类型
    protected static String SET_TABLE_NAME = "DataKeeperSet";

    protected static String SET_TABLE_CREATE_SQL =
            "DROP TABLE DataKeeperSet; CREATE TABLE \"DataKeeperSet\" (\n" +
            "  \"key\" text NOT NULL,\n" +
            "  \"value\" TEXT,\n" +
            "  \"score\" integer,\n" +
            "  \"expireTime\" integer,\n" +
            "  CONSTRAINT \"UQ_SET\" UNIQUE (\"key\", \"value\") ON CONFLICT REPLACE\n" +
            ");\n" +
            "\n" +
            "CREATE INDEX \"INDEX_SET_KEY\"\n" +
            "ON \"DataKeeperSet\" (\n" +
            "  \"key\" COLLATE BINARY ASC\n" +
            ");\n" +
            "\n" +
            "CREATE INDEX \"INDEX_SET_TIME\"\n" +
            "ON \"DataKeeperSet\" (\n" +
            "  \"expireTime\"\n" +
            ");\n" +
            "\n" +
            "CREATE TRIGGER \"ExpireSetTrigger\"\n" +
            "BEFORE INSERT\n" +
            "ON \"DataKeeperSet\"\n" +
            "BEGIN\n" +
            "  -- Type the SQL Here.\n" +
            "\t\tDelete  from DataKeeperSet where expireTime is not null and expireTime < strftime('%s','now');\n" +
            "END;";



}
