package io.github.qianzf.netty.proxy.http.handler.dencrypt;

import io.github.qianzf.netty.proxy.http.handler.client.BuildChannelInboundHandler;
import io.github.qianzf.netty.proxy.transfer.encrypt.ByteEncryptUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * Created by 18041910 on 2020/9/1.
 */
public class DecryptHandler extends ChannelInboundHandlerAdapter {

    /**
     * static logger
     */
    private static Logger logger = LoggerFactory.getLogger(BuildChannelInboundHandler.class);

    private long sendPackageLength = 0l;

    private boolean dataContent = false;


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 对写入数据进行解密
        if (msg instanceof ByteBuf) {
            int length = ((ByteBuf) msg).readableBytes();
            byte[] readBytes = new byte[length];
            ((ByteBuf) msg).readBytes(readBytes);
            // 数据解密
            Map<String, Boolean> flags = new HashMap<>();
            flags.put("dataContent", dataContent);
            readBytes = ByteEncryptUtil.decrypt(readBytes, "test", sendPackageLength, flags);
            this.dataContent = flags.get("dataContent");
            sendPackageLength += length;
            if (readBytes.length != 0) {
                ByteBuf newBuf = ctx.alloc().buffer(length);
                newBuf.writeBytes(readBytes);
                ctx.fireChannelRead(newBuf);
            }
            ReferenceCountUtil.release(msg);

        } else {
            ctx.fireChannelRead(msg);
        }

    }

}
