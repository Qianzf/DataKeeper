package io.github.qianzf.netty.proxy.transfer.handler;

import io.github.qianzf.netty.proxy.http.ServerStart;
import io.github.qianzf.netty.proxy.http.context.ChannelContext;
import io.github.qianzf.netty.proxy.http.handler.client.BuildChannelInboundHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by 18041910 on 2020/9/1.
 */
public class ProcessDataHandler extends ChannelInboundHandlerAdapter {

    /**
     * remote bootstrap
     */
    private Bootstrap remoteBootstrap;

    private Channel clientChannel;

    private Channel remoteChannel;


    private List<Object> chunk = new CopyOnWriteArrayList<>();

    /**
     * connect state
     */
    private BuildChannelInboundHandler.STATE connectState = BuildChannelInboundHandler.STATE.UNCONNECT;

    /**
     * static logger
     */
    private static Logger logger = LoggerFactory.getLogger(BuildChannelInboundHandler.class);

    @Override
    public void handlerAdded(final ChannelHandlerContext ctx) {


        clientChannel = ctx.channel();
        final String proxyHost = clientChannel.attr(ChannelContext.PROXY_HOST).get();
        final Integer proxyPort = clientChannel.attr(ChannelContext.PROXY_PORT).get();

        ChannelFuture futureChannel = connectRemote(new InetSocketAddress(proxyHost, proxyPort), new RemoteDataHandlerTransfer());
        futureChannel.addListener((ChannelFutureListener) future -> {
            // 成功建立与服务器的链接
            if (future.isSuccess()) {
                // 获取远程服务的channel
                remoteChannel = future.channel();
                // 打通两边管道的channel
                clientChannel.attr(ChannelContext.REMOTE_CHANNEL).setIfAbsent(remoteChannel);
                remoteChannel.attr(ChannelContext.CLIENT_CHANNEL).setIfAbsent(clientChannel);
                clientChannel.pipeline().addBefore("exception", "input-data-transfer", new InputDataHandlerTransfer());//dataTransfer
                castFlocks();
                connectState = BuildChannelInboundHandler.STATE.FINISHED;
            } else {
                logger.error("connect fail {}.", future.cause().getMessage());
                closeChannel();
            }
        });
    }

    /**
     * cast flocks
     */
    private void castFlocks() {
        if (!chunk.isEmpty()) {
            ChannelHandlerContext curHandlerContext = clientChannel.pipeline().context(this);
            for (Object flock : chunk) {
                curHandlerContext.fireChannelRead(flock);
            }
            chunk.clear();
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        switch (connectState) {
            case UNCONNECT:
                chunk.add(msg);
                break;
            case FINISHED:
                ctx.fireChannelRead(msg);
                break;
            default:
                super.channelRead(ctx, msg);
        }

    }



    /**
     * close both channel
     */
    private void closeChannel() {
        if (!chunk.isEmpty()) {
            for (Object flock : chunk) {
                ReferenceCountUtil.release(flock);
            }
            chunk.clear();
        }

        if (clientChannel != null) {
            clientChannel.close();
        }
        if (remoteChannel != null) {
            remoteChannel.close();
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



}
