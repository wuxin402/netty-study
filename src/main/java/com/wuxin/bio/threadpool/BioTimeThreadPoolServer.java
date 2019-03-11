package com.wuxin.bio.threadpool;

import com.wuxin.bio.BioTimeServerHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 改用线程池
 */
public class BioTimeThreadPoolServer {
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
            BioTimeServerHandlerExecutePool singleExecutor = new BioTimeServerHandlerExecutePool(50,1000);
            while (true) {
                socket = server.accept();
                singleExecutor.execute(new BioTimeServerHandler(socket));
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
    Socket的输入流进行读取操作的时候，它会一直阻塞下去，直到发生如下事件：有数据可读，可用数据已经读取完毕，发生空指针或者I/O异常。
  当发送请求或者应答消息比较缓慢，或者网络传输较慢时，读取输入流一方的通信线程将被长时间阻塞。其他接入消息只能在消息队列中排队。
    当调用OutputStream的write方法写输出流的时候，它将被阻塞，直到所有要发送的字节全部写入完毕，或者发生异常。在TCP/IP相关知识中，
  当消息的接收方处理缓慢的时候，将不能及时地从TCP缓冲区读取数据，这将会导致发送方的TCP window size不断减少，直到为0，双方处于
  Keep-Alive状态，消息发送方将不能再向TCP缓冲区写入消息，这时如果采用的是同步阻塞I/。write操作将会被无限期阻塞，直到TCP window
  size大于0或者发生I/O异常。
*/

/*
    伪异步I/O无法解决同步I/O导致的通信线程阻塞问题，引起的级联故障
    1、服务端处理缓慢，返回应答消息耗费60ms，平时只需要10ms。
    2、采用伪异步I/O的线程正在读取故障服务节点的响应，由于读取输入流是阻塞的，它将会被同步阻塞60s。
    3、假如所有的可用线程都被故障服务器阻塞，那后续所有的I/O消息都将在队列中排队。
    4、由于线程池采用阻塞队列实现，当队列积满之后，后续入队列的操作将会被阻塞。
    5、由于只有一个Accptor线程接收客户端接入，它被阻塞在线程池的同步阻塞队列之后，新的客户端请求消息将被拒绝，客户端会发生大量
    的连接超时。
    6、由于几乎所有的连接都超时，调用者会认为系统已经崩溃，无法接收新的请求消息。
 */