package com.senchuuhi.netty.proxy.transfer.handler;

import com.senchuuhi.netty.proxy.http.context.ChannelContext;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.util.ReferenceCountUtil;

import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by 18041910 on 2020/9/4.
 */
public class ProxyJudgeInboundHandler extends ChannelInboundHandlerAdapter {


    private EmbeddedChannel channel;

    private List<Object> chunks = new ArrayList<>();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof ByteBuf) {

            int length = ((ByteBuf) msg).readableBytes();
            byte[] readBytes = new byte[length];
            ((ByteBuf) msg).readBytes(readBytes);
            // 根据原始数据生成两笔拷贝，一笔用于http数据的判断，一笔用于下游传递
            ByteBuf buf = ctx.alloc().buffer(length);
            buf.writeBytes(readBytes);
            ByteBuf buf2 = ctx.alloc().buffer(length);
            buf2.writeBytes(readBytes);
            if (this.channel == null) {
                this.channel = new EmbeddedChannel(new HttpRequestDecoder());
            }
            this.channel.writeInbound(buf);
            Object request = channel.readInbound();
            if (request != null && request instanceof HttpRequest) {
                InetSocketAddress address = getDstAddress((HttpRequest)request);
                System.out.println(matchInnerNet(address) + "|" + ((HttpRequest) request).uri());
                if (matchInnerNet(address)) {
                    // 设置代理地址A
                    ctx.channel().attr(ChannelContext.PROXY_HOST).setIfAbsent("127.0.0.1");
                    ctx.channel().attr(ChannelContext.PROXY_PORT).setIfAbsent(1081);
                    ctx.channel().attr(ChannelContext.PASS_ENCRYPT).setIfAbsent(true);
                } else if (matchGFWNet(address)) {
                    // 设置代理地址B
                    ctx.channel().attr(ChannelContext.PROXY_HOST).setIfAbsent("127.0.0.1");
                    ctx.channel().attr(ChannelContext.PROXY_PORT).setIfAbsent(1080);
                    ctx.channel().attr(ChannelContext.PASS_ENCRYPT).setIfAbsent(false);
                } else {
                    // 设置代理地址C
                    ctx.channel().attr(ChannelContext.PROXY_HOST).setIfAbsent("10.37.235.10");
                    ctx.channel().attr(ChannelContext.PROXY_PORT).setIfAbsent(8080);
                    ctx.channel().attr(ChannelContext.PASS_ENCRYPT).setIfAbsent(false);
                }
                ctx.pipeline().addBefore("exception", "ProcessData", new ProcessDataHandler());

                if (!chunks.isEmpty()) {
                    chunks.forEach(x-> ctx.fireChannelRead(x));
                }
                // 触发下级读写
                ctx.fireChannelRead(buf2);
                // 释放自己
                ctx.pipeline().remove(this);

            } else {
                chunks.add(buf2);
            }
            ReferenceCountUtil.release(msg);
        } else {
            ctx.fireChannelRead(msg);
        }

    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ctx.fireChannelInactive();
        if (this.channel != null) {
            this.channel.close();
        }

    }

    /**
     * 内网匹配
     * @return
     */
    private boolean matchInnerNet(InetSocketAddress dstAddress) {
        String host = dstAddress.getHostString();

        String[] hosts = {"oatest.pptv.com","oa.pptv.com","securelogin.arubanetworks.com","127.0.0.1","192.168.","172.","localhost","10.",".cnsuning.com",".cnsuny.com",".suning.com",".suning.cn",".chinadq.com",".coobery.com",".suningshop.com",".suningestate.cn",".suningestate.com",".uninu.com.cn",".sn.suning.ad",".realhostip.com",".suningplaza.com",".suningproperty.com",".cloudytrace.com",".cloudytrace.cn",".yifubao.com",".suningcloud.com",".snjijin.com",".suningssc.com",".synacast.local",".financesn.com",".suningdangjian.com",".snp2p.com",".snxiaojinshi.com",".suningholdings.com",".blockx.cn",".snisc.cn",".suningyunyou.com",".suningrpa.com"};

        for (String x : hosts) {
            if (host.startsWith(x) || host.endsWith(x)) {
                return true;
            }
        }
        return false;

    }

    private boolean matchGFWNet(InetSocketAddress dstAddress) {
        String host = dstAddress.getHostString();

        String[] hosts = {"google.com", "google-analytics.com", "github.com", "githubassets.com", "githubapp.com", "googleapi.com"};

        for (String x : hosts) {
            if (host.startsWith(x) || host.endsWith(x)) {
                return true;
            }
        }
        return false;
    }

    private InetSocketAddress getDstAddress(HttpRequest httpMsg) {
        InetSocketAddress result = null;

        String uri = httpMsg.uri();
        if (uri.startsWith("http://") || uri.startsWith("https://")) {
            try {
                URL url = new URL(uri);
                result = new InetSocketAddress(url.getHost(), url.getPort() == -1 ? 80 : url.getPort());
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException(httpMsg.uri() + " is getDstAddress fail");
            }
        } else {
            String host = uri.contains(":") ? uri.substring(0, uri.lastIndexOf(":")) : uri;
            int port = uri.contains(":") ? Integer.valueOf(uri.substring(uri.lastIndexOf(":") + 1)) : 80;
            return new InetSocketAddress(host, port);
        }

        return result;
    }

}
