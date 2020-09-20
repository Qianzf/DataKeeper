package com.senchuuhi.datakeeper.utils;

/**
 * Created by QQQZF on 2020/9/20.
 */
public class FileUtils {


    /**
     * 获取基础文件保存路径的地址
     * @return
     */
    public static String getBasePath() {
        // 获取全路径
        String path = FileUtils.class.getProtectionDomain().getCodeSource().getLocation().getPath();

        if (System.getProperty("os.name").contains("dows")) {
            path = path.substring(1, path.length());
        }
        if (path.contains("jar")) {
            path = path.substring(0, path.lastIndexOf("."));
            return path.substring(0, path.lastIndexOf("/"));
        }
        return path.replace("target/classes/", "");
    }
}
