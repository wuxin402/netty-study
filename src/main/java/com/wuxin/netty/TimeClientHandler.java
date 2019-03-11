package com.wuxin.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

import java.util.logging.Logger;

/**
 * 客户端处理器
 */
public class TimeClientHandler extends ChannelHandlerAdapter {
    private static final Logger logger = Logger.getLogger(TimeClientHandler.class.getName());

    private final ByteBuf firstMessage;

    /**
     * create a client-side handler
     */
    public TimeClientHandler() {
        byte[] req = "QUERY TIME ORDER".getBytes();
        firstMessage = Unpooled.buffer(req.length);
        firstMessage.writeBytes(req);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush(firstMessage);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf =(ByteBuf)msg;
        byte[] req = new byte[buf.readableBytes()];
        buf.readBytes(req);
        String body = new String(req,"UTF-8");
        System.out.println("Now is:"+body);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //释放资源
        logger.warning("Unexpected exception from downstream:"+cause.getMessage());
        ctx.close();
    }

    /**
     *1、当客户端和服务端TCP链路建立成功之后，Netty的NIO线程会调用channelActive方法，发送查询时间的
     *指令给服务端，调用ChannelHandlerContext的writeAndFlush方法将请求消息发送给服务端。
     *2、当服务端返回应答消息时，channelRead方法被调用，打印服务端返回的消息。
     *3、发生异常时，打印异常日志，释放客户端资源。
     */
}
