package com.wuxin.netty.decoder.echo;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

/**
 * 服务处理器
 */
public class EchoServerHandler extends ChannelHandlerAdapter {
    int counter = 0;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String body = (String) msg;
        System.out.println("This is"+ ++counter + "times receive client:["+body+"]");
        body += "$_";
        ByteBuf echo = Unpooled.copiedBuffer(body.getBytes());
        ctx.writeAndFlush(echo);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    /**
     * 1、DelimiterBaseFrameDecoder自动对请求消息进行了解码,后续对ChannelHandler接收到到msg对象
     * 就是个完整的消息包。第二个ChannelHandler是StringDecoder,它将ByteBuf解码成字符串对象。
     * 第三个EchoServerHandler接收到的msg消息就是解码后的字符串对象。
     * 2、设置DelimiterBasedFrameDecoder过滤掉了分隔符,所以,返回给客户端时需要在请求消息尾部拼接
     * 分隔符"$_",最后创建ByteBuf,将原始消息重新返回给客户端。
     */
}
