package io.github.qianzf.netty.proxy.http.context;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.util.AttributeKey;

import java.net.InetSocketAddress;
import java.util.Queue;

public class ChannelContext {

    public enum PROTOCOL {
        HTTP,
        TUNNEL,
        PROXY
    }
    public static AttributeKey<PROTOCOL> TRANSPORT_PROTOCOL = AttributeKey.valueOf("TRANSPORT_PROTOCOL");
    public static AttributeKey<String> PROXY_HOST = AttributeKey.valueOf("PROXY_HOST");
    public static AttributeKey<Integer> PROXY_PORT = AttributeKey.valueOf("PROXY_PORT");
    public static AttributeKey<Boolean> PASS_ENCRYPT = AttributeKey.valueOf("PASS_ENCRYPT");
    public static AttributeKey<InetSocketAddress> DST_ADDRESS = AttributeKey.valueOf("DST_ADDRESS");
    public static AttributeKey<HttpRequest> REQUEST_DATA = AttributeKey.valueOf("REQUEST_DATA");
    public static AttributeKey<Queue<HttpRequest>> REMOTE_QUERY_HTTP = AttributeKey.valueOf("REMOTE_QUERY_HTTP");
    // 用于在管道里面传递数据
    public static AttributeKey<Channel> REMOTE_CHANNEL = AttributeKey.valueOf("REMOTE_CHANNEL");

    public static AttributeKey<Channel> CLIENT_CHANNEL = AttributeKey.valueOf("CLIENT_CHANNEL");

    public static AttributeKey<Boolean> DATA_CONTENT = AttributeKey.valueOf("DATA_CONTENT");
}
