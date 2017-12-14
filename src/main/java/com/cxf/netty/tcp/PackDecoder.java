package com.cxf.netty.tcp;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import com.cxf.logger.Logs;

//@ChannelHandler.Sharable
public class PackDecoder extends FixedDecoder {

    public PackDecoder() {
        super(16 * 1024, 1, 2, 2, 0);
    }

    public PackDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment, int initialBytesToStrip) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip, false);

    }

    @Override
    protected final Object decode(ChannelHandlerContext ctx, ByteBuf in) {
        try {
            int readIndex = in.readerIndex();
            int length = in.readableBytes();

            in.readerIndex(readIndex);

            String str = Integer.toHexString(in.readByte() & 0xFF).toUpperCase();
            Logs.TCP.error("head:" + str);
            int index = 0;
            while (!str.equals("EC")) {
                if (length > 1) {
                    length--;
                    str = Integer.toHexString(in.readByte() & 0xFF).toUpperCase();

                    // Logs.TCP.error(str + "dataStr:" + length);

                } else {
                    return null;
                }

            }

            try {
                Object obj = super.decode(ctx, in);
                if (obj != null) {

                    // Logs.TCP.error(str + ":objstr:" + length);
                    return obj;
                }
            } catch (Exception e) {
                in.readerIndex(readIndex);
                Logs.TCP.error("msg error  exception={}", e);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
