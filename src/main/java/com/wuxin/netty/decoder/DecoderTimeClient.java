package com.wuxin.netty.decoder;

import com.wuxin.netty.decoder.echo.EchoClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;

/**
 * 处理粘包和拆包的client
 */
public class DecoderTimeClient {
    public void connect(int port,String host) throws Exception{
        //配置客户端NIO线程组
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group).channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY,true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new LineBasedFrameDecoder(1024));
                            socketChannel.pipeline().addLast(new StringDecoder());
                            socketChannel.pipeline().addLast(new DecoderTimeClientHandler());
                        }
                    });
            //发起异步连接操作
            ChannelFuture f = b.connect(host,port).sync();

            //等待客户端链路关闭
            f.channel().closeFuture().sync();
        } finally {
            //优雅退出，释放NIO线程组
            group.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception{
        int port = 8080;
        new DecoderTimeClient().connect(port,"127.0.0.1");
    }

    /**
     * 1、LineBasedFrameDecoder的工作原理是它依次遍历ByteBuf中的可读字节，判断看是否有"\n"或者"\r\n"，
     * 如果有，就以此位置为结束位置，从可读索引到结束位置区间的字节就组成了一行。它是以换行符为结束标志的解码器，
     * 支持携带结束符或者不携带结束符两种解码方式，同时支持配置单行的最大长度。如果连续读取到最大长度后仍然没有
     * 发现换行符，就会抛出异常，同时忽略掉之前读到到异常码流。
     * 2、StringDecoder的功能非常简单，就是将接收到到对象转换成字符串，然后继续调用后面到Handler。LineBasedFrameDecoder
     * +StringDecoder组合就是按行切换的文本解码器，它被设计用来支持TCP的粘包和拆包。
     */
    public static class EchoClient {
        public void connect(int port,String host) throws Exception{
            //配置客户端NIO线程组
            EventLoopGroup group = new NioEventLoopGroup();
            try {
                Bootstrap b = new Bootstrap();
                b.group(group).channel(NioSocketChannel.class)
                        .option(ChannelOption.TCP_NODELAY,true)
                        .handler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel socketChannel) throws Exception {
                                ByteBuf delimiter = Unpooled.copiedBuffer("$_".getBytes());
                                socketChannel.pipeline().addLast(new DelimiterBasedFrameDecoder(1024,delimiter));
                                socketChannel.pipeline().addLast(new StringDecoder());
                                socketChannel.pipeline().addLast(new EchoClientHandler());
                            }
                        });
                //发起异步连接操作
                ChannelFuture f = b.connect(host,port).sync();

                //等待客户端链路关闭
                f.channel().closeFuture().sync();
            } finally {
                //优雅退出，释放NIO线程组
                group.shutdownGracefully();
            }
        }

        public static void main(String[] args) throws Exception{
            int port = 8080;
            new EchoClient().connect(port,"127.0.0.1");
        }
    }
}
