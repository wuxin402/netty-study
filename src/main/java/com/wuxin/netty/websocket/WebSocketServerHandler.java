package com.wuxin.netty.websocket;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.CharsetUtil;

import java.util.Date;

import static io.netty.handler.codec.http.HttpUtil.isKeepAlive;
import static io.netty.handler.codec.http.HttpUtil.setContentLength;

public class WebSocketServerHandler extends SimpleChannelInboundHandler<Object> {
    private WebSocketServerHandshaker handshaker;

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, Object msg) throws Exception {
        /*传统的HTTP接入*/
        if (msg instanceof FullHttpRequest) {
            handleHttpRequest(ctx,(FullHttpRequest)msg);
        } else if (msg instanceof WebSocketFrame) {  /*Websocket接入*/
            handWebSocketFrame(ctx,(WebSocketFrame)msg);
        }
    }

    private void handWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame msg) {
        //判断是否是关闭链路的指令
        if (msg instanceof CloseWebSocketFrame) {
            handshaker.close(ctx.channel(),((CloseWebSocketFrame) msg).retain());
        }

        //判断是否是Ping消息
        if (msg instanceof PingWebSocketFrame) {
            ctx.channel().write(new PongWebSocketFrame(msg.content().retain()));
            return;
        }

        //仅支持文本消息，不支持二进制消息
        if (!(msg instanceof TextWebSocketFrame)) {
            throw new UnsupportedOperationException(String.format("%s frame types not supported",msg.getClass().getName()));
        }

        //返回应答消息
        String request = ((TextWebSocketFrame)msg).text();
        ctx.channel().write(new TextWebSocketFrame(request + ", 欢迎使用Netty Websocket服务，现在时刻：" + new Date().toString()));
    }

    private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest msg) {
        //如果HTTP解码失败，返回HTTP异常
        if (!msg.getDecoderResult().isSuccess()
            || (!"websocket".equals(msg.headers().get("Upgrade")))) {
            sendHttpResponse(ctx,msg,new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST));

            //构造握手响应返回，本机测试
            WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory("ws://localhsot:8080/websockert",null,false);
            handshaker = wsFactory.newHandshaker(msg);
            if (handshaker == null) {
               WebSocketServerHandshakerFactory.sendUnsupportedWebSocketVersionResponse(ctx.channel());
            } else {
                handshaker.handshake(ctx.channel(),msg);
            }
        }


    }

    private void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req, DefaultFullHttpResponse resp) {
        //返回应答给客户端
        if (resp.getStatus().code() != 200) {
            ByteBuf buf = Unpooled.copiedBuffer(resp.getStatus().toString(), CharsetUtil.UTF_8);
            resp.content().writeBytes(buf);
            buf.release();
            setContentLength(resp,resp.content().readableBytes());
        }

        //如果是非Keep-Alive,关闭连接
        ChannelFuture f = ctx.channel().writeAndFlush(resp);
        if (!isKeepAlive(req) || resp.getStatus().code() != 200) {
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
