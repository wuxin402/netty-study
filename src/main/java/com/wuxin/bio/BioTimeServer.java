package com.wuxin.bio;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 服务端
 */
public class BioTimeServer {
    public static void main(String[] args) throws IOException {
        int port = 8080;
        if (args.length > 0) {
            try {
                port = Integer.valueOf(args[0]);
            } catch (NumberFormatException e) {
                port = 8080;
            }
        }
        ServerSocket server = null;
        try {
            server = new ServerSocket(port);
            System.out.println("The time server is start in port;"+port);
            Socket socket = null;
            while (true) {
                socket = server.accept();
                new Thread(new BioTimeServerHandler(socket)).start();
            }
        } finally {
            if (server != null) {
                System.out.println("The time server close");
                server.close();
                server = null;
            }
        }
    }
}

/*
    BIO网络模型端主要问题在于每点那个有一个新的客户端请求接入时，服务端必须创建一个新的线程处理新接入的客户端
    链路，一个线程只能处理一个客户端连接。在高性能服务器应用领域，往往需要面向成千上万个客户端端并发连接，这种
    模型显然无法满足高性能、高并发接入的场景。
 */