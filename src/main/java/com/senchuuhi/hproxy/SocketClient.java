package com.senchuuhi.hproxy;

import com.alibaba.fastjson.JSONObject;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by 18041910 on 2020/8/31.
 */
public class SocketClient {

    static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(10, 10, 200l, TimeUnit.SECONDS, new LinkedBlockingDeque<>(2000));

    public static void main(String[] s) {

        ServerSocket ss = null;
        try {
            ss = new ServerSocket(1088);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        while (true) {
            try {
                // 接收来自客户端的链接
                final Socket socket = ss.accept();
                // 设置代理服务器与客户端的连接未活动超时时间
                socket.setSoTimeout(1000 * 60);
                // 使用线程池来进行逐步处理
                threadPoolExecutor.execute(() -> {
                    String line = "";
                    InputStream is = null;
                    try {
                        is = socket.getInputStream();
                        String tempHost = "", host;
                        int port = 80;
                        String type = null;

                        OutputStream os = socket.getOutputStream();
                        // 创建到目标服务器的地址
                        Socket proxySocket = new Socket(SocketServer.SocketServerHost, SocketServer.SocketServerPort);
                        // 设置代理服务器与服务器端的连接未活动超时时间
                        proxySocket.setSoTimeout(1000 * 60);
                        OutputStream proxyOs = proxySocket.getOutputStream();
                        InputStream proxyIs = proxySocket.getInputStream();

                        //监听客户端传来消息并转发给服务器
                        new ProxyHandleThread(is, proxyOs, "").start();
                        //监听服务器传来消息并转发给客户端
                        new ProxyHandleThread(proxyIs, os, "").start();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

}
