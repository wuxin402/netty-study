package com.wuxin.netty.decoder.msgpack;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * msgpack服务端
 */
public class MsgpackServer {
    private final int port;

    public  MsgpackServer(int port) {
        this.port = port;
    }
    public void run() throws Exception {
        EventLoopGroup acceptorGroup = new NioEventLoopGroup();
        EventLoopGroup IOGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(acceptorGroup, IOGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 100)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast("frameDecoder", new LengthFieldBasedFrameDecoder(65535, 0, 2, 0, 2));
                            ch.pipeline().addLast("msgpack decoder", new MsgpackDecoder());
                            ch.pipeline().addLast("frameEncoder", new LengthFieldPrepender(2));
                            ch.pipeline().addLast("msgpack encoder", new MsgpackEncoder());
                            ch.pipeline().addLast(new MsgServerHandler());
                        }
                    });
            ChannelFuture future = b.bind(port).sync();
            future.channel().closeFuture().sync();
        } finally {
            acceptorGroup.shutdownGracefully();
            IOGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception{
        int port = 8080;
        new MsgpackServer(port).run();
    }
}
