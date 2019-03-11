package com.wuxin.nio;

/**
 * NIO的服务端
 */
public class NioTimeServer {
    public static void main(String[] args) {
        int port = 8080;
        NioMultiplexerTimeServer timeServer = new NioMultiplexerTimeServer(port);
        new Thread(timeServer,"NIO-MultiplexerTimeServer-001").start();
    }
}
