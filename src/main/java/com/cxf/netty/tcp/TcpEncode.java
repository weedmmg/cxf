package com.cxf.netty.tcp;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.io.DataOutputStream;
import java.io.OutputStream;

@ChannelHandler.Sharable
public final class TcpEncode extends MessageToByteEncoder<byte[]> {

    public static final TcpEncode INSTANCE = new TcpEncode();

    @Override
    protected void encode(ChannelHandlerContext ctx, byte[] msg, ByteBuf out) throws Exception {
        // int startIdx = out.writerIndex();

        ByteBufOutputStream bout = new ByteBufOutputStream(out);

        byte[] head = new byte[1];

        int length = msg.length;

        OutputStream oout = new DataOutputStream(bout);
        oout.write(msg);
        oout.flush();
        oout.close();

    }

}
