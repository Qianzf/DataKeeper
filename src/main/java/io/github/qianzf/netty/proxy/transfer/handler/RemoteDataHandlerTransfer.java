package io.github.qianzf.netty.proxy.transfer.handler;

import io.github.qianzf.netty.proxy.http.context.ChannelContext;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * Created by 18041910 on 2020/9/1.
 */
@ChannelHandler.Sharable
public class RemoteDataHandlerTransfer extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ctx.channel().attr(ChannelContext.CLIENT_CHANNEL).get().writeAndFlush(msg);
    }
}
