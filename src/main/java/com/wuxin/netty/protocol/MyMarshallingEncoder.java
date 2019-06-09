package com.wuxin.netty.protocol;

import io.netty.buffer.ByteBuf;
import org.jboss.marshalling.ByteBufferOutput;
import org.jboss.marshalling.Marshaller;

import java.io.IOException;


public class MyMarshallingEncoder {
    private static final byte[] LENGTH_PLACEHOLDER = new byte[4];
    Marshaller marshaller;

    public MyMarshallingEncoder() throws IOException {
        marshaller = MarshallingCodeCFactory.builderMarshalling();
    }

    protected void encode (Object msg, ByteBuf out) throws Exception{
        try {
            int lengthPos = out.writerIndex();
            out.writeBytes(LENGTH_PLACEHOLDER);
            marshaller.start(new ByteBufferOutput(out.nioBuffer()));
            marshaller.writeObject(msg);
            marshaller.finish();
            out.setInt(lengthPos,out.writerIndex() - lengthPos - 4);
        }  finally {
            marshaller.close();
        }
    }
}
