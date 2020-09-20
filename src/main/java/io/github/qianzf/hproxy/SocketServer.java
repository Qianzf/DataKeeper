package io.github.qianzf.hproxy;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by 18041910 on 2020/8/31.
 */
public class SocketServer {


    static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(10, 10, 200l, TimeUnit.SECONDS, new LinkedBlockingDeque<>(2000));

    public static String SocketServerHost = "127.0.0.1";

    public static int SocketServerPort = 1089;

    public static void main(String[] args) {
        final ServerSocket server;
        try {
            server = new ServerSocket(SocketServerPort);
            System.out.println(String.format("服务端启动：%s：%s", SocketServerHost, SocketServerPort));
            while(true) {
                Socket socket = server.accept();
                threadPoolExecutor.execute(() -> {
                    String line = "";
                    InputStream is = null;
                    try {
                        is = socket.getInputStream();
                        String tempHost = "", host;
                        int port = 80;
                        String type = null;

                        OutputStream os = socket.getOutputStream();
                        BufferedReader br = new BufferedReader(new InputStreamReader(is));
                        int temp = 1;
                        StringBuilder sb = new StringBuilder();
                        while ((line = br.readLine()) != null) {
                            if (temp == 1) {
                                //获取请求行中请求方法，下面会需要这个来判断是http还是https
                                type = line.split(" ")[0];
                                if (type == null) continue;
                            }
                            temp++;
                            String[] s1 = line.split(": ");
                            if (line.isEmpty()) {
                                break;
                            }
                            for (int i = 0; i < s1.length; i++) {
                                if (s1[i].equalsIgnoreCase("host")) {
                                    tempHost = s1[i + 1];
                                }
                            }
                            sb.append(line + "\r\n");
                        }
                        // 不加上这行http请求则无法进行。这其实就是告诉服务端一个请求结束了
                        sb.append("\r\n");
                        System.out.println("sb:\n" + sb);
                        System.out.println("--------------------------");

                        if (tempHost.split(":").length > 1) {
                            port = Integer.valueOf(tempHost.split(":")[1]);
                        }
                        host = tempHost.split(":")[0];
                        System.out.println("host:\n" + host);
                        System.out.println("port:\n" + port);
                        System.out.println("--------------------------");

                        if (host != null && !host.equals("")) {
                            // 创建到目标服务器的地址
                            Socket proxySocket = new Socket(host, port);
                            // 设置代理服务器与服务器端的连接未活动超时时间
                            proxySocket.setSoTimeout(1000 * 60);
                            OutputStream proxyOs = proxySocket.getOutputStream();
                            InputStream proxyIs = proxySocket.getInputStream();
                            if (type.equalsIgnoreCase("connect")) {     //https请求的话，告诉客户端连接已经建立（下面代码建立）
                                os.write("HTTP/1.1 200 Connection Established\r\n\r\n".getBytes());
                                os.flush();
                            } else {//http请求则直接转发
                                proxyOs.write(sb.toString().getBytes("utf-8"));
                                proxyOs.flush();
                            }
                            //监听客户端传来消息并转发给服务器
                            new ProxyHandleThread(is, proxyOs, host).start();
                            //监听服务器传来消息并转发给客户端
                            new ProxyHandleThread(proxyIs, os, host).start();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                });

            }

        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
