package com.wuxin.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * netty作为服务端
 */
public class TimeServer {
    public void bind(int port) throws Exception{
        //配置服务端端NIO线程组
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup,workerGroup).channel(NioServerSocketChannel.class)
                                          .option(ChannelOption.SO_BACKLOG,1024)
                                          .childHandler(new ChildChannelHandler());
            //绑定端口，同步等待成功
            ChannelFuture f = b.bind(port).sync();

            //等待服务端监听端口关闭
            f.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    private class ChildChannelHandler extends ChannelInitializer<SocketChannel> {

        @Override
        protected void initChannel(SocketChannel socketChannel) throws Exception {
            socketChannel.pipeline().addLast(new TimeServerHandler());
        }
    }

    public static void main(String[] args) throws Exception{
        int port = 8080;
        new TimeServer().bind(port);
    }

    /**
     * 1、创建两个NioEventLoopGroup实例，NioEventLoopGroup是个线程组，它包含了一组NIO线程，专门
     * 用于网络事件当处理，实际上它们就是Reactor线程组，这里创建两个的理由是一个用于服务端接受客户端的
     * 连接，另一个用于进行SocketChannel的网络读写。
     * 2、创建ServerBootstrap对象，它是Netty启动NIO服务端的辅助启动类，目的是降低服务端的开发复杂度，
     * ServerBootstrap的group方法，将两个NIO线程组当作入参传递到ServerBootstrap中，然后设置创建
     * 的Channel为NioServerSocketChannel,对应的功能为ServerSocketChannel类。
     * 3、配置NioServerSocketChannel的TCP参数，设置backlog的大小为1024，绑定I/O事件的处理类
     * ChildChannelHandler,它的作用类似于Reactor模式中的Handler类，主要用于处理网络I/O事件，
     * 例如记录日志、对消息进行编解码等。
     * 4、服务端启动辅助类配置完成之后，调用它的bind方法绑定监听端口，然后调用它的同步阻塞方法sync等待
     * 绑定操作完成。完成之后Netty会返回一个ChannelFuture，它的功能类似于JDK的java.util.concurrent.Future,
     * 主要用于异步操作的通知回调。
     * 5、f.channel().closeFuture().sync()方法进行阻塞，等待服务端链路关闭之后main函数才退出。
     * 6、调用NIO线程组的shutdownGracefully进行优雅退出，它会释放跟shutdownGracefully相关联的资源。
     */
}
