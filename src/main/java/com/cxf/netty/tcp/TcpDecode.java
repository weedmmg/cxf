package com.cxf.netty.tcp;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;

public class TcpDecode extends MessageToMessageDecoder<ByteBuf> {

    @Override
    protected final void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        try {
            System.out.println("this is read");
            int length = in.readableBytes();
            byte[] array = new byte[length];
            in.getBytes(0, array);

            // // 心跳包
            // if (array.length == 1) {
            // int readerIndex = in.readerIndex();
            // ByteBuf frame = extractFrame(ctx, in, readerIndex, 1,
            // array.length);
            // addByte(frame, out);
            // }
            addByte(in, out);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addByte(ByteBuf in, List<Object> out) {
        if (in == null) {
            return;
        }
        byte[] array = new byte[in.readableBytes()];
        in.getBytes(0, array);
        out.add(array);
    }

    protected ByteBuf extractFrame(ChannelHandlerContext ctx, ByteBuf buffer, int index, int length, int dataLength) {
        if (dataLength < length) {
            return null;
        }
        return buffer.slice(index, length).retain();
    }
}
