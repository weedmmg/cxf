package com.cxf.netty.tcp;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import com.cxf.logger.Logs;
import com.cxf.util.ByteUtil;

//@ChannelHandler.Sharable
public class PackDecoder extends LengthFieldBasedFrameDecoder {

    public PackDecoder() {
        super(16 * 1024, 2, 1, 2, 0);
    }

    public PackDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment, int initialBytesToStrip) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);

    }

    @Override
    protected final Object decode(ChannelHandlerContext ctx, ByteBuf in) {
        int readIndex = in.readerIndex();

        try {
            Object obj = super.decode(ctx, in);
            if (obj != null) {
                return obj;
            }
        } catch (Exception e) {
            in.readerIndex(readIndex);
            int length = in.readableBytes();
            byte[] array = new byte[length];
            in.getBytes(0, array);
            e.printStackTrace();
            Logs.TCP.warn("receive msg:" + ByteUtil.printHexString(array));
            Logs.TCP.warn("receive msg length:" + length);
        }
        return null;
    }
}
