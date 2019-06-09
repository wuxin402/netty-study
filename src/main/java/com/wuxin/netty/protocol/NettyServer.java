package com.wuxin.netty.protocol;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;

public class NettyServer {
    public void bind() throws Exception{
        //配置服务端对NIO线程组
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup,workerGroup).channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG,100)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    protected void initChannel(SocketChannel sc) throws Exception {
                        sc.pipeline().addLast(new NettyMessageDecoder(1024*1024,4,4));
                        sc.pipeline().addLast(new NettyMessageEncoder());
                        sc.pipeline().addLast("readTimeoutHandler",new ReadTimeoutHandler(50));
                        sc.pipeline().addLast(new LoginAuthRespHandler());
                        sc.pipeline().addLast("HeartBeatHandler",new HeartBeatRespHandler());
                    }
                });
        //绑定端口，同步等待成功
        b.bind("127.0.0.1",8080).sync();
        System.out.println("Netty server start ok:" );
    }

    public static void main(String[] args) throws Exception{
        new NettyServer().bind();
    }
}
