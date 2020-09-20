package io.github.qianzf.netty.proxy.transfer.encrypt;

import com.alibaba.fastjson.JSONObject;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.bytes.ByteList;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 *  字节加密
 * Created by 18041910 on 2020/9/1.
 */
public class ByteEncryptUtil {


    /**
     * 获取
     * @param origin
     * @param key
     * @return
     */
    public static byte[] encrypt(final byte[] origin, String key, long startLength, Map<String, Integer> param) {

        byte[] keyBytes;
        byte[] randomBytes = null;
        int baseLength = 0;
        if (startLength == 0 && origin.length != 0) {
            randomBytes = getRandomBytes();
            baseLength = randomBytes.length;
        }

        int totalLength = origin.length + baseLength;
        byte[] result = new byte[totalLength];

        try {

            // 随机数组和原数组合并
            keyBytes = key.getBytes("UTF-8");
            for (int i = 0; i < result.length; ++i) {
                int temp = (int) ((startLength + i) % keyBytes.length);
                if (i < baseLength) {
                    result[i] = (byte) (keyBytes[temp] ^ randomBytes[i]);
                } else {
                    result[i] = (byte) (keyBytes[temp] ^ origin[i - baseLength]);
                }
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        param.put("finalLength", totalLength);

        return result;
    }


    /**
     * 获取
     * @param origin
     * @param key
     * @return
     */
    public static byte[] decrypt(final byte[] origin, String key, long startLength, Map<String, Boolean> flags) {

        final byte[] keyBytes;

        boolean dataContent = flags.get("dataContent");
        ByteList byteList = new ByteArrayList();
        try {
            keyBytes = key.getBytes("UTF-8");
            int length = keyBytes.length;
            // 数组遍历
            for (int i = 0; i < origin.length; ++i) {
                int temp = (int) ((startLength + i) % length);
                int value = origin[i] ^ keyBytes[temp] ;
                if (dataContent) {
                    byteList.add((byte) (value));
                }
                if (value == 14 && !dataContent) {
                    dataContent = true;
                    flags.put("dataContent", true);
                }
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return byteList.toByteArray();
    }


    public static byte[] getRandomBytes() {
        Random nextRandom = new Random();
        int ifNext = nextRandom.nextInt(100);
        ByteList byteList = new ByteArrayList();
        while (ifNext < 80) {
            Random r = new Random();
            int i = r.nextInt(254);
            if (i != 14) {
                byteList.add((byte)i);
            }
            ifNext = nextRandom.nextInt(100);

        }
        byteList.add((byte)14);

        return byteList.toByteArray();
    }

    public static void main(String[] args) {
        for (int i = 0; i < 1000; ++i) {
            System.out.println(JSONObject.toJSONString(getRandomBytes()));
        }

    }
}
