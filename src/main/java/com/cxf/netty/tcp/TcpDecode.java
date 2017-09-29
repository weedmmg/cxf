package com.cxf.netty.tcp;

import java.util.List;

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
