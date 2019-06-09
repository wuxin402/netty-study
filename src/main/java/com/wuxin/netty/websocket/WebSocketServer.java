package com.wuxin.netty.websocket;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * webSocket服务端
 */
public class WebSocketServer {
    public void run(int port) throws Exception{
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup,workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            pipeline.addLast("http-codec",new HttpServerCodec());
                            pipeline.addLast("aggregator",new HttpObjectAggregator(65536));
                            pipeline.addLast("http-chunked",new ChunkedWriteHandler());
//                            pipeline.addLast("handler",new WebSocketServerHandler());
                        }
                    });
            Channel ch = b.bind(port).sync().channel();
            System.out.println("Web socket server started at port" + port +".");
            System.out.println("Open your brower and navigate to http://localhost:"+port+"/");
            ch.closeFuture().sync();

        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        int port = 8080;
        new WebSocketServer().run(port);
    }

    /**
     * HttpServerCodec将请求和应答消息编码或者解码为HTTP消息；
     * HttpObjectAggregator将HTTP消息的多个部分组合成一条完整的HTTP消息；
     * ChunkedWriteHandler将向客户端发送HTML5文件，主要用于支持浏览器和服务端进行WebSocket通信。
     */
}
