package com.wuxin.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

import java.util.Date;


/**
 * 处理器
 */
public class TimeServerHandler extends ChannelHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        byte[] req = new byte[buf.readableBytes()];
        buf.readBytes(req);
        String body = new String(req,"UTF-8");
        System.out.println("The time server receive order: "+body);
        String currentTime = "QUERY TIME ORDER".equalsIgnoreCase(body)?new Date().toString():"BAD ORDER";
        ByteBuf resp = Unpooled.copiedBuffer(currentTime.getBytes());
        ctx.write(resp);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }

    /**
     *1、将msg转换成Netty当ByteBuf对象，ByteBuf类似于JDK中的java.nio.ByteBuffer对象，不过它
     *更强大和灵活，通过ByteBuf的readableBytes方法可以获取缓冲区可读的字节数，根据可读的字节数创建
     *byte数组，通过ByteBuf的readBytes方法将缓冲区中的字节数组复制到新建的byte数组中，最后通过
     *new String构造函数获取请求消息。
     *2、ChannelHandlerContext的flush方法，它的作用时将消息发送队列中的消息写入到SocketChannel中
     *发送给对方。从性能角度考虑，为了防止频繁地唤醒Selector进行消息发送，Netty的write方法并不直接将
     *消息写入SocketChannel中，调用write方法只是把待发送的消息放到发送缓冲数组中，再通过调用flush
     *方法，将发送缓冲区中的消息全部写到SocketChannel。
     *3、当发生异常时，关闭ChannelHandlerContext,释放和ChannleHandlerContext
     */
}
