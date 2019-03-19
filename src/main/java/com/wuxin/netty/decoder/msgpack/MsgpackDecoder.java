package com.wuxin.netty.decoder.msgpack;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import org.msgpack.MessagePack;

import java.util.List;

/**
 * msgpack解码器
 */
public class MsgpackDecoder extends MessageToMessageDecoder<ByteBuf> {

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        final byte[] array;
        final int length = byteBuf.readableBytes();
        array = new byte[length];
        byteBuf.getBytes(byteBuf.readerIndex(),array,0,length);
        MessagePack msgpack = new MessagePack();
        list.add(msgpack.read(array,UserInfo.class));
    }

    /**
     * 首先从数据包ByteBuf中获取需要解码到byte数组,然后调用MessagePack到read方法将其反
     * 序列化为Object对象，将解码后的对象加入到list中，这样就完成了解码操作
     */
}
