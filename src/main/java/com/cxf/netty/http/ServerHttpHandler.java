package com.cxf.netty.http;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;

@Sharable
public class ServerHttpHandler extends ChannelHandlerAdapter {

	private static Logger logger = LoggerFactory.getLogger(ServerHttpHandler.class);

	private HttpRequest request;

	public ServerHttpHandler() {
		super();
	}

	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {

	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		// cause.printStackTrace();
		ctx.close();
	}

	@Override
	public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
		ctx.channel().close();
		logger.debug("HTTP连接断开");

	}

	protected Object decode(ByteBuf in) throws Exception {
		byte[] bytes;
		int length = in.readableBytes();

		if (in.hasArray()) {
			bytes = in.array();
		} else {
			bytes = new byte[length];
			in.getBytes(in.readerIndex(), bytes);
		}
		return bytes;
	}

	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		if (msg instanceof HttpContent) {
			HttpContent httpContent = (HttpContent) msg;

			ByteBuf content = httpContent.content();
			String returnCode = "ok";

			byte[] bytes;
			try {
				bytes = (byte[]) decode(content);
				String message = new String(bytes, "UTF-8");
				logger.info("接收信息:" + message);
			} catch (Exception e) {
				returnCode = "error";
				logger.error(" 消息接收失败：" + e.getMessage());
			}
			FullHttpResponse response;
			try {
				response = new DefaultFullHttpResponse(HTTP_1_1, OK,
						Unpooled.wrappedBuffer(returnCode.getBytes("UTF8")));
				response.headers().set(CONTENT_TYPE, "text/plain");
				response.headers().setInt(CONTENT_LENGTH, response.content().readableBytes());

				ctx.writeAndFlush(response);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}

	}

	private String getParamerByNameFromGET(String name) {
		QueryStringDecoder decoderQuery = new QueryStringDecoder(request.uri());
		return getParameterByName(name, decoderQuery);
	}

	/**
	 * 根据传入参数的key获取value
	 * 
	 * @param name
	 * @param decoderQuery
	 * @return
	 */
	private String getParameterByName(String name, QueryStringDecoder decoderQuery) {
		Map<String, List<String>> uriAttributes = decoderQuery.parameters();
		for (Entry<String, List<String>> attr : uriAttributes.entrySet()) {
			String key = attr.getKey();
			for (String attrVal : attr.getValue()) {
				if (key.equals(name)) {
					return attrVal;
				}
			}
		}
		return null;
	}
}
