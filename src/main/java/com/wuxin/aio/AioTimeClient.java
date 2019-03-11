package com.wuxin.aio;

/**
 * 异步客户端
 */
public class AioTimeClient {

    public static void main(String[] args) {
        int port = 8080;
        new Thread(new AioAsyncTimeClientHandler("127.0.0.1",port),"AIO-AsyncTimeClientHandler-001").start();
    }
}
