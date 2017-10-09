package com.cxf.netty.tcp;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;

import com.cxf.logger.Logs;
import com.cxf.util.ByteUtil;

public class TcpDecode extends MessageToMessageDecoder<ByteBuf> {

    @Override
    protected final void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        try {
            int length = in.readableBytes();
            byte[] array = new byte[length];
            in.getBytes(0, array);
            Logs.TCP.warn("receive msg:" + ByteUtil.printHexString(array));

            int index = 0;
            while (!Integer.toHexString(in.readByte() & 0xFF).toUpperCase().equals("EC")) {
                index++;
            }
            Logs.TCP.warn("index:" + index);
            ByteBuf cmdBytes = in.slice(1, 1), lenBytes = in.slice(2, 1);

            byte[] cmds = new byte[1], lens = new byte[1];

            cmdBytes.readBytes(cmds);
            lenBytes.readBytes(lens);

            int len = ByteUtil.byteArrayToInt(lens, 1);
            byte[] newArray;
            ByteBuf msgByte;

            switch (cmds[0]) {
            case 0x01:
                newArray = new byte[5 + len];
                msgByte = in.slice(0, 5 + len);
                msgByte.readBytes(newArray);
                out.add(newArray);
                break;

            case (byte) 0xBE:
                newArray = new byte[5 + len];
                msgByte = in.slice(0, 5 + len);
                msgByte.readBytes(newArray);
                out.add(newArray);
                break;
            default:
                in.skipBytes(5 + len);
                break;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
