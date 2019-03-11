package com.wuxin.aio;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

public class AioAcceptCompletionHandler implements CompletionHandler<AsynchronousSocketChannel,AioAsyncTimeServerHandler> {

    @Override
    public void completed(AsynchronousSocketChannel result, AioAsyncTimeServerHandler attachment) {
        attachment.asynchronousServerSocketChannel.accept(attachment,this);
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        result.read(buffer,buffer,new AioReadCompletionHandler(result));
    }

    @Override
    public void failed(Throwable exc, AioAsyncTimeServerHandler attachment) {
        attachment.latch.countDown();
    }

    /**
     * 调用AsynchronousServerSocketChannel的accept方法后，如果有新的客户端连接接入，系统将回调我们
     * 传入的CompletionHandler实例的completed方法，表示新的客户端已经接入成功。因为一个AsynchronousServerSocketChannel
     * 可以接收成千上万个客户端，所以需要继续调用它的accept方法，接收其他的客户端连接，最终形成一个循环。没当
     * 接收一个客户读连接成功之后，再异步接收新的客户端连接。
     */
}
