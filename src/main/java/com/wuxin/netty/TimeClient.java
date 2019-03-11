package com.wuxin.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * netty 客户端
 */
public class TimeClient {
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
                                socketChannel.pipeline().addLast(new TimeClientHandler());
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
        new TimeClient().connect(port,"127.0.0.1");
    }

    /**
     *1、创建客户端处理I/O都写的NioEventLoopGroup线程组，然后继续创建客户端辅助启动类Bootstrap，
     *随后需要对其进行配置，与服务端不同的时，它的channel需要设置为NioSockChannel,然后为其添加
     *Handler。直接传教匿名内部类，实现initChannel方法，其作用是当创建NioSockChannel成功之后，
     *在进行初始化时，将它的ChannelHandler设置到ChannelPipeline中，用于处理网络I/O事件。
     *2、客户端启动辅助类设置完成之后，调用connect方法发起异步连接，然后调用同步方法等待连接成功。
     *3、最后，当客户端连接关闭之后，客户端主函数退出，退出之前释放NIO线程组当资源。
     */
}
