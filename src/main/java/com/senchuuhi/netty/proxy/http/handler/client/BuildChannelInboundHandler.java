package com.senchuuhi.netty.proxy.http.handler.client;


import com.senchuuhi.netty.proxy.http.ServerStart;
import com.senchuuhi.netty.proxy.http.context.ChannelContext;
import com.senchuuhi.netty.proxy.http.handler.remote.DefaultRemoteFlowTransfer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static com.senchuuhi.netty.proxy.http.context.ChannelContext.PROTOCOL.PROXY;


/**
 * connect or reconnect remote channel and finish handshake
 *
 * @author zk
 * @since 2019/7/9
 */
public class BuildChannelInboundHandler extends ChannelInboundHandlerAdapter {
    /**
     * static logger
     */
    private static Logger logger = LoggerFactory.getLogger(BuildChannelInboundHandler.class);
    /**
     * client channel
     */
    private Channel clientChannel;
    /**
     * remote channel
     */
    private Channel remoteChannel;
    /**
     * connect state
     */
    private STATE connectState = STATE.UNCONNECT;
    /**
     * remote bootstrap
     */
    private Bootstrap remoteBootstrap;

    private List<Object> flocks = new LinkedList<>();

    @Override
    public void handlerAdded(final ChannelHandlerContext ctx) {
        setClientChannel(ctx.channel());
        //build connect
        final InetSocketAddress dstAddress = clientChannel.attr(ChannelContext.DST_ADDRESS).get();

        final ChannelContext.PROTOCOL protocol = clientChannel.attr(ChannelContext.TRANSPORT_PROTOCOL).get();

        final HttpRequest requestData = clientChannel.attr(ChannelContext.REQUEST_DATA).get();

        final ChannelFuture channelFuture;

        switch (protocol) {
            case HTTP:
                // 建立和远程服务器的链接remoteChannel
                channelFuture = connectRemote(dstAddress, new HttpRequestEncoder(), new DefaultRemoteFlowTransfer(),  new ExceptionDuplexHandler());
                channelFuture.addListener((ChannelFutureListener) future -> {
                    if (future.isSuccess()) {
                        setRemoteChannel(future.channel());
                        logger.info("[{}-{}] type: http, channel connect [{}:{}] success.", clientChannel.id(), remoteChannel.id(), dstAddress.getHostName(), dstAddress.getPort());
                        // 打通两边管道的channel
                        clientChannel.attr(ChannelContext.REMOTE_CHANNEL).setIfAbsent(remoteChannel);
                        remoteChannel.attr(ChannelContext.CLIENT_CHANNEL).setIfAbsent(clientChannel);
                        // 移除响应编码器
                        clientChannel.pipeline().remove(HttpResponseEncoder.class);
                        // 添加数据交换的处理器
                        clientChannel.pipeline().addLast(new DefaultClientFlowTransfer());//dataTransfer
                        connectState = STATE.FINISHED;//connect success
                        // 触发下次
                        castFlocks();//fire next read
                    } else {
                        logger.error("[{}-]connect [{}:{}] fail {}.", clientChannel.id(), dstAddress.getHostName(), dstAddress.getPort(), future.cause().getMessage());
                        closeChannel();
                    }
                });
                break;
            case TUNNEL:
                channelFuture = connectRemote(dstAddress, new DefaultRemoteFlowTransfer(),  new ExceptionDuplexHandler());
                channelFuture.addListener((ChannelFutureListener) future -> {
                    if (future.isSuccess()) {
                        setRemoteChannel(future.channel());
                        logger.info("[{}-{}] type: tunnel, channel connect [{}:{}] success.", clientChannel.id(), remoteChannel.id(), dstAddress.getHostName(), dstAddress.getPort());

                        clientChannel.attr(ChannelContext.REMOTE_CHANNEL).setIfAbsent(remoteChannel);
                        remoteChannel.attr(ChannelContext.CLIENT_CHANNEL).setIfAbsent(clientChannel);
                        clientChannel.pipeline().addBefore("exception", "client-flower-transfer", new DefaultClientFlowTransfer());//dataTransfer

                        connectState = STATE.FINISHED;//connect success
                        //response ack
                        clientChannel.writeAndFlush(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK))
                                .addListener((ChannelFutureListener) future1 -> {
                                    clientChannel.pipeline().remove(HttpRequestDecoder.class);
                                    clientChannel.pipeline().remove(HttpResponseEncoder.class);
                        });

                    } else {
                        logger.error("[{}-] connect [{}:{}] fail {}.", clientChannel.id(), dstAddress.getHostName(), dstAddress.getPort(), future.cause().getMessage());
                        closeChannel();
                    }
                });
                break;

            default:
                throw new IllegalArgumentException("protocol type is undefined " + protocol);
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        switch (connectState) {
            case UNCONNECT:
                flocks.add(msg);
                break;
            case FINISHED:
                ctx.fireChannelRead(msg);
                break;
            default:
                super.channelRead(ctx, msg);
        }
    }

    /**
     * connect remote server
     *
     * @param dstAddress      remote server address
     * @param channelHandlers handlers
     * @return future
     */
    private ChannelFuture connectRemote(InetSocketAddress dstAddress, final ChannelHandler... channelHandlers) {
        if (remoteBootstrap == null) {
            remoteBootstrap = new Bootstrap();
        }
        return remoteBootstrap.group(clientChannel.eventLoop())
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) ServerStart.serverConfigure.getTimeout())
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<Channel>() {
                    protected void initChannel(Channel ch) {
                        for (ChannelHandler channelHandler : channelHandlers) {
                            ch.pipeline().addLast(channelHandler);
                        }
                    }
                }).connect(dstAddress);
    }


    /**
     * cast flocks
     */
    private void castFlocks() {
        if (!flocks.isEmpty()) {
            ChannelHandlerContext curHandlerContext = clientChannel.pipeline().context(this);
            for (Object flock : flocks) {
                curHandlerContext.fireChannelRead(flock);
            }
            flocks.clear();
        }
    }

    /**
     * close both channel
     */
    private void closeChannel() {
        if (!flocks.isEmpty()) {
            for (Object flock : flocks) {
                ReferenceCountUtil.release(flock);
            }
            flocks.clear();
        }

        if (clientChannel != null) {
            clientChannel.close();
        }
        if (remoteChannel != null) {
            remoteChannel.close();
        }
    }


    /**
     * set clientChannel
     *
     * @param clientChannel clientChanel
     */
    private void setClientChannel(Channel clientChannel) {
        this.clientChannel = clientChannel;
    }

    /**
     * set remote Channel
     *
     * @param remoteChannel remoteChannel
     */
    private void setRemoteChannel(Channel remoteChannel) {
        this.remoteChannel = remoteChannel;
    }

    public enum STATE {
        /**
         * un connect
         */
        UNCONNECT,
        /**
         * connected
         */
        FINISHED
    }
}
