package com.cxf.netty.tcp;

import java.io.DataOutputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cxf.util.ByteUtil;
import com.cxf.util.MD5Util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

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

		String headStr = "EX", endStr = "86";

		byte[] head = headStr.getBytes(encoding), end = endStr.getBytes(encoding);

		String sign = MD5Util.string2MD5(new String(msg));
		logger.debug("sign:" + sign);
		byte[] newMsg = ByteUtil.byteMergerAll(head, msg, sign.getBytes(encoding), end);

		logger.debug("加密前前" + msg.length);
		logger.debug("加密后" + newMsg.length);
		OutputStream oout = new DataOutputStream(bout);
		oout.write(newMsg);
		oout.flush();
		oout.close();
	}

}
