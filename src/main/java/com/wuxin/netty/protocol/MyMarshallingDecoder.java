package com.wuxin.netty.protocol;


import io.netty.buffer.ByteBuf;
import org.jboss.marshalling.ByteBufferInput;
import org.jboss.marshalling.ByteInput;
import org.jboss.marshalling.Unmarshaller;

import java.io.IOException;

public class MyMarshallingDecoder {
    private final Unmarshaller unmarshaller;


    public MyMarshallingDecoder() throws IOException {
        unmarshaller = MarshallingCodeCFactory.builderUnMarshalling();
    }

    protected Object decode(ByteBuf in) throws Exception{
        int objectSize = in.readInt();
        ByteBuf buf = in.slice(in.readerIndex(),objectSize);
        ByteInput input = new ByteBufferInput(in.nioBuffer());
        try {
            unmarshaller.start(input);
            Object obj = unmarshaller.readObject();
            unmarshaller.finish();
            in.readerIndex(in.readerIndex() + objectSize);
            return obj;
        } finally {
            unmarshaller.close();
        }
    }
}
