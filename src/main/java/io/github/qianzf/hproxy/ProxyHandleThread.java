package io.github.qianzf.hproxy;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketTimeoutException;

/**
 * 交换代理数据
 * Created by 18041910 on 2020/8/31.
 */
public class ProxyHandleThread extends Thread {

    private InputStream input;
    private OutputStream output;
    private String host;//debug时需要的东西，实际不需要

    public ProxyHandleThread(InputStream input, OutputStream output, String host) {
        this.input = input;
        this.output = output;
        this.host = host;
    }

    @Override
    public void run() {
        try {
            while (true) {
                BufferedInputStream bis = new BufferedInputStream(input);
                byte[] buffer = new byte[1024];
                int length = -1;
                // 这里最好是字节转发，不要用上面的InputStreamReader，因为https传递的都是密文，那样会乱码，消息传到服务器端也会出错。
                while ((length = bis.read(buffer)) != -1) {
                    // 加密数据转换器
                    output.write(buffer, 0, length);
                    length = -1;
                }

            }
        } catch (SocketTimeoutException e) {
            try {
                input.close();
                output.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } catch (IOException e) {
            System.out.println(e);
        } finally {
            try {
                input.close();
                output.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}