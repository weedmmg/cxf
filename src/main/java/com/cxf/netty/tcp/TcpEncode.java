package com.cxf.netty.tcp;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.io.DataOutputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cxf.logger.Logs;
import com.cxf.util.ByteUtil;

@ChannelHandler.Sharable
public final class TcpEncode extends MessageToByteEncoder<byte[]> {

    public static final TcpEncode INSTANCE = new TcpEncode();

    private static Logger logger = LoggerFactory.getLogger(TcpEncode.class);

    private static String encoding = "utf8";

    @Override
    protected void encode(ChannelHandlerContext ctx, byte[] msg, ByteBuf out) throws Exception {
        // int startIdx = out.writerIndex();

        ByteBufOutputStream bout = new ByteBufOutputStream(out);

        byte[] head = new byte[1];

        int length = msg.length;

        writeMsg(msg, bout);

    }

    public static void writeMsg(byte[] msg, ByteBufOutputStream bout) throws Exception {

        byte[] head = ByteUtil.hexString2Bytes("EC"), end = ByteUtil.hexString2Bytes("68");

        // String sign = MD5Util.string2MD5(new String(msg));

        byte[] signs = ByteUtil.sumCheck(msg, 1);
        // logger.debug("sign:" + sign);
        ByteBuf byteBuf = Unpooled.wrappedBuffer(head, msg, signs, end);

        byte[] array = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(array);
        Logs.TCP.warn("send msg:" + ByteUtil.printHexString(array));
        OutputStream oout = new DataOutputStream(bout);
        oout.write(array);
        oout.flush();
        oout.close();
    }

}
