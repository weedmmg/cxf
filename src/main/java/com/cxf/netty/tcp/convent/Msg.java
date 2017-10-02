package com.cxf.netty.tcp.convent;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.cxf.logger.Logs;
import com.cxf.netty.connection.Connection;
import com.cxf.netty.connection.ConnectionManager;
import com.cxf.netty.tcp.NettyTCPServer;
import com.cxf.thread.UrlThreadFactory;
import com.cxf.util.ByteUtil;
import com.cxf.util.MD5Util;
import com.cxf.util.PropertiesUtil;
import com.cxf.util.Strings;

public class Msg {

    private static String encoding = "utf8";
    private static Logger logger = LoggerFactory.getLogger(Msg.class);
    private final static byte errorCmd = 0x04;
    private final static byte successCmd = 0x02;

    public static byte[] conventMsg(Object msg, ChannelHandlerContext ctx, ConnectionManager connectionManager) throws UnsupportedEncodingException {
        ByteBuf totalBytes = Unpooled.wrappedBuffer((byte[]) msg);
        ByteBuf cmdBytes = totalBytes.slice(1, 1), lenBytes = totalBytes.slice(2, 1);

        byte[] cmds = new byte[1], lens = new byte[1];

        cmdBytes.readBytes(cmds);
        lenBytes.readBytes(lens);

        int totalLengh = totalBytes.readableBytes(), len = ByteUtil.byteArrayToInt(lens, 1), signLen = totalLengh - 4 - len;

        ByteBuf dataBytes = totalBytes.slice(3, len), signBytes = totalBytes.slice(3 + len, signLen), checkBytes = totalBytes.slice(1, 2 + len);
        byte[] datas = new byte[len];
        dataBytes.readBytes(datas);
        byte[] checkBytesArray = new byte[2 + len], signBytesArray = new byte[signLen];

        checkBytes.readBytes(checkBytesArray);
        signBytes.readBytes(signBytesArray);

        if (checkSign(checkBytesArray, signBytesArray)) {
            logger.error("sign error");
            return intMsg(errorCmd, "sign error".getBytes(encoding));
        }
        handleMsg(cmds[0], datas, ctx, connectionManager);
        return intMsg(successCmd, "ok".getBytes(encoding));

    }

    public static void handleMsg(byte cmd, byte[] data, ChannelHandlerContext ctx, ConnectionManager connectionManager) {

        logger.info("channelId:" + ctx.channel().id());
        Map<String, Object> paramMap = new HashMap<String, Object>();
        String url = "", channelId = "";

        logger.debug("cmd:" + Byte.toString(cmd));
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
            paramMap.put("serverIp", PropertiesUtil.getValue("netty.ip"));

            if (ctx != null) {
                ctx.executor().submit(new UrlThreadFactory(url, new JSONObject(paramMap).toString()));
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

            if (ctx != null) {

                paramMap.put("channelId", ctx.channel().id().toString());
                paramMap.put("serverIp", PropertiesUtil.getValue("netty.ip"));
                initLocationMap(paramMap, data);
                ctx.executor().submit(new UrlThreadFactory(url, new JSONObject(paramMap).toString()));
            }

            channelId = ctx.channel().attr(NettyTCPServer.rcvChannel).get();
            if (!Strings.isBlank(channelId)) {
                try {
                    Connection rcvConnection = connectionManager.getById(channelId);

                    if (rcvConnection != null) {

                        rcvConnection.send(new TextWebSocketFrame(new JSONObject(paramMap).toString()));
                        Logs.WS.info("send location msg:" + new JSONObject(paramMap).toString());
                    }

                    break;
                } catch (Exception e) {
                    Logs.WS.error("push modify time error:" + e.getMessage());
                }
            }

            break;
        default:
            logger.error("error cmd" + Byte.toString(cmd));
            break;
        }
    }

    public static void initLocationMap(Map<String, Object> paramMap, byte[] data) {
        ByteBuf dataBytes = Unpooled.wrappedBuffer(data), stateBytes = dataBytes.slice(0, 1), nssBytes = dataBytes.slice(1, 1), ewsBytes = dataBytes.slice(2, 1), speedBytes = dataBytes.slice(3, 2), llBytes = dataBytes
                .slice(5, 4), laBytes = dataBytes.slice(9, 4), statusBytes = dataBytes.slice(13, 1);
        // TODO 传输定位信息
        byte[] states = new byte[1],
        // nss = new byte[1], ews = new byte[1],
        speeds = new byte[2], lls = new byte[4], las = new byte[4], status = new byte[1];

        stateBytes.readBytes(states);
        // dataBytes.readBytes(nss, 1, 1);
        // dataBytes.readBytes(ews, 2, 1);
        speedBytes.readBytes(speeds);
        llBytes.readBytes(lls);
        laBytes.readBytes(las);
        statusBytes.readBytes(status);
        paramMap.put("gpsstate", ByteUtil.byteArrayToLong(states, 1));
        paramMap.put("longitude", (double) ByteUtil.byteArrayToLong(lls, 4) / 1000000);
        paramMap.put("latitude", (double) ByteUtil.byteArrayToLong(las, 4) / 1000000);
        paramMap.put("speed", ByteUtil.byteArrayToLong(speeds, 2));
        paramMap.put("gpsstatus", ByteUtil.byteArrayToLong(status, 1));
        paramMap.put("uuid", "123456");
    }

    public static boolean checkSign(byte[] msg, byte[] sign) {
        String msgSign = MD5Util.string2MD5(new String(msg));

        if (msgSign.equals(new String(sign))) {
            return false;
        }
        return true;
    }

    public static byte[] intMsg(byte cmd, byte[] data) {

        ByteBuf bf = Unpooled.wrappedBuffer(initCmd(cmd), initLen(data.length), data);
        byte[] array = new byte[bf.readableBytes()];
        bf.readBytes(array);
        return array;

    }

    public static byte[] initCmd(byte b) {
        byte[] bytes = new byte[1];
        bytes[0] = b;
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

            System.out.println(Msg.conventMsg(newMsg, null, null));

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }
}
