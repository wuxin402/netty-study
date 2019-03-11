package com.wuxin.aio;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Date;

public class AioReadCompletionHandler implements CompletionHandler<Integer, ByteBuffer> {
    private AsynchronousSocketChannel channel;

    public AioReadCompletionHandler(AsynchronousSocketChannel channel) {
        if (this.channel == null) {
            this.channel = channel;
        }
    }

    @Override
    public void completed(Integer result, ByteBuffer attachment) {
        attachment.flip();
        byte[] body = new byte[attachment.remaining()];
        attachment.get(body);
        try {
            String req = new String(body,"UTF-8");
            System.out.println("The time server receive order:"+req);
            String currentTime = "QUERY TIME ORDER".equalsIgnoreCase(req)?new Date().toString():"BAD ORDER";
            doWrite(currentTime);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private  void doWrite (String currentTime) {
        if (currentTime != null && currentTime.trim().length() > 0) {
            byte[] bytes =currentTime.getBytes();
            ByteBuffer writeBuffer = ByteBuffer.allocate(bytes.length);
            writeBuffer.put(bytes);
            writeBuffer.flip();
            channel.write(writeBuffer, writeBuffer, new CompletionHandler<Integer, ByteBuffer>() {
                @Override
                public void completed(Integer result, ByteBuffer buffer) {
                    //如果没有发送完成，继续发送
                    if (buffer.hasRemaining()) {
                        channel.write(buffer,buffer,this);
                    }
                }

                @Override
                public void failed(Throwable exc, ByteBuffer attachment) {
                    try {
                        channel.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
    @Override
    public void failed(Throwable exc, ByteBuffer attachment) {
        /**
         * 当发生异常当时候，对异常对Throwable进行判断，如果是I/O异常，就关闭链路，释放资源，如果是
         * 其他异常，按照业务自己对逻辑进行处理。
         */
        try {
            channel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
