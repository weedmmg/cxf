package com.cxf.netty.tcp.convent;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.cxf.thread.UrlThreadFactory;
import com.cxf.util.ByteUtil;
import com.cxf.util.MD5Util;
import com.cxf.util.PropertiesUtil;
import com.cxf.util.Strings;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.EventExecutor;

public class Msg {
	private static String encoding = "utf8";
	private static Logger logger = LoggerFactory.getLogger(Msg.class);
	private final static byte errorCmd = 0x04;
	private final static byte successCmd = 0x02;

	public static byte[] conventMsg(Object msg, ChannelHandlerContext ctx) throws UnsupportedEncodingException {
		byte[] bytes = (byte[]) msg, heads = new byte[2], cmds = new byte[1], lens = new byte[1], ends = new byte[2];

		// 处理头EX
		System.arraycopy(bytes, 0, heads, 0, 2);
		// 处理CMD
		System.arraycopy(bytes, 2, cmds, 0, 1);
		// 处理LEN
		System.arraycopy(bytes, 3, lens, 0, 1);

		int len = ByteUtil.byteArrayToInt(lens, 1), signLen = bytes.length - 6 - len;
		byte[] data = new byte[len], sign = new byte[signLen];
		// 处理DATA
		System.arraycopy(bytes, 4, data, 0, len);
		// 处理SIGN
		System.arraycopy(bytes, 4 + len, sign, 0, signLen);
		// 处理END
		System.arraycopy(bytes, 4 + len + signLen, ends, 0, 2);

		if (!new String(heads).equals("EX") || !new String(ends).equals("86")) {
			logger.error("head or ends error");
			return intMsg(errorCmd, "head or ends error".getBytes(encoding));
		}
		if (checkSign(ByteUtil.byteMergerAll(cmds, lens, data), sign)) {
			logger.error("sign error");
			return intMsg(errorCmd, "sign error".getBytes(encoding));
		}
		handleMsg(cmds[0], data, ctx);
		return intMsg(successCmd, new byte[0]);

	}

	public static void handleMsg(byte cmd, byte[] data, ChannelHandlerContext ctx) {
		Map<String, Object> paramMap = new HashMap<String, Object>();
		String url = "";
		switch (cmd) {
		case 0x01:
			// regist
			String uuid = new String(data);
			if (Strings.isBlank(uuid)) {
				logger.error("error UUID is null:" + uuid);
				break;
			}

			url = PropertiesUtil.getValue("system.register.url");
			if (Strings.isBlank(url)) {
				logger.error("error system.register.url is null:" + url);
				break;
			}

			paramMap.put("uuid", uuid);
			paramMap.put("channelId", ctx.channel().id().toString());

			if (ctx != null) {
				EventExecutor loop = ctx.executor();
				loop.submit(new UrlThreadFactory(url, new JSONObject(paramMap).toString()));
			}
			break;
		case 0x05:
			// heartbeat
			break;
		case (byte) 0xBE:

			// location
			url = PropertiesUtil.getValue("system.location.url");
			if (Strings.isBlank(url)) {
				logger.error("error system.location.url is null:" + url);
				break;
			}

			// paramMap.put("uuid", uuid);
			paramMap.put("channelId", ctx.channel().id().toString());

			if (ctx != null) {
				EventExecutor loop = ctx.executor();
				loop.submit(new UrlThreadFactory(url, new JSONObject(paramMap).toString()));
			}

			break;
		default:
			logger.error("error cmd" + Byte.toString(cmd));
			break;
		}
	}

	public static boolean checkSign(byte[] msg, byte[] sign) {
		String msgSign = MD5Util.string2MD5(new String(msg));

		if (msgSign.equals(new String(sign))) {
			return false;
		}
		return true;
	}

	public static byte[] intMsg(byte cmd, byte[] data) {

		return ByteUtil.byteMergerAll(initCmd(cmd), initLen(data.length), data);

	}

	public static byte[] initCmd(byte b) {
		byte[] bytes = new byte[1];
		bytes[0] = 0x01;
		return bytes;
	}

	public static byte[] initLen(int size) {
		logger.debug("len:" + size);
		return ByteUtil.intToByteArray(size, 1);
	}

	public static void main(String[] args) {
		String headStr = "EX", endStr = "86";
		byte cmd = 0x01;
		String uuid = UUID.randomUUID().toString();
		logger.debug(uuid);
		byte[] head, end, msg;
		try {
			head = headStr.getBytes(encoding);
			end = endStr.getBytes(encoding);
			msg = intMsg(cmd, uuid.getBytes(encoding));

			String sign = MD5Util.string2MD5(new String(msg));

			byte[] newMsg = ByteUtil.byteMergerAll(head, msg, sign.getBytes(encoding), end);

			logger.debug(sign);
			logger.debug("加密后" + newMsg.length);
			System.out.println("len:" + newMsg.toString().length());

			System.out.println(Msg.conventMsg(newMsg, null));

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

	}
}