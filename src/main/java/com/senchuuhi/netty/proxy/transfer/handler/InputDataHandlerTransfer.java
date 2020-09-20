package com.senchuuhi.netty.proxy.transfer.handler;

import com.senchuuhi.netty.proxy.http.context.ChannelContext;
import com.senchuuhi.netty.proxy.http.handler.client.BuildChannelInboundHandler;
import com.senchuuhi.netty.proxy.transfer.encrypt.ByteEncryptUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by 18041910 on 2020/9/1.
 */
public class InputDataHandlerTransfer extends ChannelInboundHandlerAdapter {


    /**
     * static logger
     */
    private static Logger logger = LoggerFactory.getLogger(BuildChannelInboundHandler.class);

    private long sendPackageLength = 0l;


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        final boolean isEncrypt = ctx.channel().attr(ChannelContext.PASS_ENCRYPT).get();
        // 对写入数据进行加密
        if (msg instanceof ByteBuf && isEncrypt) {
            int length = ((ByteBuf) msg).readableBytes();
            byte[] readBytes = new byte[length];
            ((ByteBuf) msg).readBytes(readBytes);
            Map<String, Integer> param = new HashMap<>();
            readBytes = ByteEncryptUtil.encrypt(readBytes, "test", sendPackageLength, param);
            sendPackageLength += readBytes.length;
            ByteBuf buf = Unpooled.copiedBuffer(readBytes);
            ctx.channel().attr(ChannelContext.REMOTE_CHANNEL).get().writeAndFlush(buf);
            ReferenceCountUtil.release(msg);
        } else {
            ctx.channel().attr(ChannelContext.REMOTE_CHANNEL).get().writeAndFlush(msg);
        }


    }


}
