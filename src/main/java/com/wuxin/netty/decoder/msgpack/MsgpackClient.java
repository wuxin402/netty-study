package com.wuxin.netty.decoder.msgpack;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;

/**
 * 利用msgpack到客户端　
 */
public class MsgpackClient {
    private final String host;
    private final int port;
    private final int sendNumber;

    public MsgpackClient(String host,int port,int sendNumber) {
        this.host = host;
        this.port = port;
        this.sendNumber = sendNumber;
    }

    public void run() throws Exception{
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY,true)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS,3000)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast("frameDecoder",new LengthFieldBasedFrameDecoder(65535,0,2,0,2));
                            socketChannel.pipeline().addLast("msgpack decoder",new MsgpackDecoder());
                            socketChannel.pipeline().addLast("frameEncoder",new LengthFieldPrepender(2));
                            socketChannel.pipeline().addLast("msgpack encoder",new MsgpackEncoder());
                            socketChannel.pipeline().addLast(new MsgpackClientHandler(sendNumber));
                        }
                    });
            ChannelFuture future = b.connect(host, port).sync();
            future.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception{
        int port = 8080;
        new MsgpackClient("127.0.0.1",port,100).run();
    }
}
