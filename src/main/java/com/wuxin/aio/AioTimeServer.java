package com.wuxin.aio;

/**
 * AIO服务端
 */
public class AioTimeServer {
    public static void main(String[] args) {
        int port = 8080;
        AioAsyncTimeServerHandler timeServer = new AioAsyncTimeServerHandler(port);
        new Thread(timeServer,"AIO-AioAsyncTimeServerHandler-001").start();
    }
}
