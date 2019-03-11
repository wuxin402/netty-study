package com.wuxin.nio;

/**
 * nio客户端
 */
public class NioTimeClient {
    public static void main(String[] args) {
        int port = 8080;
        new Thread(new NioTimeClientHandle("127.0.0.1",port),"TimeClient-001").start();
    }
}
