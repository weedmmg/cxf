package com.cxf.netty.tcp;

import java.util.List;

import com.cxf.logger.Logs;
import com.cxf.util.ByteUtil;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

public class TcpDecode extends MessageToMessageDecoder<ByteBuf> {

	@Override
	protected final void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
		try {
			int length = in.readableBytes();
			byte[] array = new byte[length];
			in.getBytes(0, array);
			Logs.TCP.warn("receive msg:" + ByteUtil.printHexString(array));
			String prex = Integer.toHexString(array[0] & 0xFF).toUpperCase();
			String end = Integer.toHexString(array[array.length - 1] & 0xFF).toUpperCase();
			if (!Integer.toHexString(array[0] & 0xFF).toUpperCase().equals("EC")) {
				// 如果不是EX开头则跳过
				in.skipBytes(1);
				return;
			}

			if (Integer.toHexString(array[array.length - 1] & 0xFF).toUpperCase().equals("68")) {

				Logs.TCP.warn("receive msg:" + ByteUtil.printHexString(array));
				out.add(array);

			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
