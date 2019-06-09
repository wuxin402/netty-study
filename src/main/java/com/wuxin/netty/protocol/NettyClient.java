package com.wuxin.netty.protocol;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class NettyClient {
    private ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    EventLoopGroup group = new NioEventLoopGroup();

    public void connect(int port,String host) throws Exception{
        //配置客户端NIO线程组
        try {
            Bootstrap b = new Bootstrap();
            b.group(group).channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY,true)
                    .handler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel sc) throws Exception {
                            sc.pipeline().addLast(new NettyMessageDecoder(1024*1024,4,4));
                            sc.pipeline().addLast("MessageEncoder",new NettyMessageEncoder());
                            sc.pipeline().addLast("readTimeOutHandler",new ReadTimeoutHandler(50));
                            sc.pipeline().addLast("LoginAuthHandler",new LoginAuthReqHandler());
                            sc.pipeline().addLast("HeartBeatHandler",new HeartBeatReqHandler());
                        }
                    });
            //发起异步连接操作
            ChannelFuture future = b.connect(new InetSocketAddress(host,port)).sync();
            future.channel().closeFuture().sync();
        } finally {
            //所有资源释放完成之后，清空资源，再次发起重连操作
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        TimeUnit.SECONDS.sleep(5);
                        connect(8080,"127.0.0.1");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    public static void main(String[] args) throws Exception{
        new NettyClient().connect(8080,"127.0.0.1");
    }

    /**
     * NettyMessageDecoder用于Netty消息解码，为了防止由于单条消息过大导致的内存溢出或者畸形码流导致解码
     * 错位引起内存分配失败，对单条消息最大长度进行了上限限制。
     * 随后依次增加了读超时Handler、握手请求Handler和心跳消息Handler。
     */
}
