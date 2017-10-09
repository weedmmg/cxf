package com.cxf.netty.tcp.convent;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

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

    private static final String NUM_FORMAT = "00000";
    private static final DecimalFormat df = new DecimalFormat(NUM_FORMAT);

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
        handleMsg(cmds[0], datas, ctx, connectionManager, len);
        return intMsg(successCmd, "ok".getBytes(encoding));

    }

    public static void handleMsg(byte cmd, byte[] data, ChannelHandlerContext ctx, ConnectionManager connectionManager, int len) {

        // logger.info("channelId:" + ctx.channel().id());
        Map<String, Object> paramMap = new HashMap<String, Object>();
        String url = "", channelId = "";

        logger.debug("cmd:" + Byte.toString(cmd));
        switch (cmd) {
        case 0x01:
            // regist
            String uuid = "" + ByteUtil.byteArrayToInt(data, len);
            if (Strings.isBlank(uuid)) {
                logger.error("error UUID is null:" + uuid);
                break;
            }
            url = PropertiesUtil.getValue("system.register.url");
            if (Strings.isBlank(url)) {
                logger.error("error system.register.url is null:" + url);
                break;
            }

            ctx.channel().attr(NettyTCPServer.uid).set(uuid);
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
                paramMap.put("uuid", ctx.channel().attr(NettyTCPServer.uid).get());
                logger.debug("result:" + new JSONObject(paramMap).toString());
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
        ByteBuf dataBytes = Unpooled.wrappedBuffer(data), stateBytes = dataBytes.slice(0, 1), nssBytes = dataBytes.slice(1, 1), ewsBytes = dataBytes.slice(2, 1), speedBytes1 = dataBytes.slice(3, 1), speedBytes2 = dataBytes
                .slice(4, 1), llBytes1 = dataBytes.slice(5, 1), llBytes2 = dataBytes.slice(6, 3), laBytes1 = dataBytes.slice(9, 1), laBytes2 = dataBytes.slice(10, 3);
        // TODO 传输定位信息
        byte[] states = new byte[1], nss = new byte[1], ews = new byte[1], speeds1 = new byte[1], speeds2 = new byte[1], lls1 = new byte[1], lls2 = new byte[3], las1 = new byte[1], las2 = new byte[3];

        stateBytes.readBytes(states);
        nssBytes.readBytes(nss);
        ewsBytes.readBytes(ews);
        speedBytes1.readBytes(speeds1);
        speedBytes2.readBytes(speeds2);
        llBytes1.readBytes(lls1);
        laBytes1.readBytes(las1);
        llBytes2.readBytes(lls2);
        laBytes2.readBytes(las2);
        // logger.debug("ss:" + ByteUtil.printHexString(lls1) + "." + "ss:" +
        // ByteUtil.printHexString(lls2));
        float speeds;
        try {
            speeds = (float) (ByteUtil.byteArrayToInt(speeds1, 1) * 100 + ByteUtil.byteArrayToInt(speeds2, 1)) / 100;
        } catch (Exception e) {
            speeds = 0;
            logger.error(e.getMessage());
        }

        BigDecimal lls = new BigDecimal(ByteUtil.byteArrayToInt(lls1, 1) + "." + df.format(ByteUtil.byteArrayToInt(lls2, 3)));
        BigDecimal las = new BigDecimal(ByteUtil.byteArrayToInt(las1, 1) + "." + df.format(ByteUtil.byteArrayToInt(las2, 3)));
        // statusBytes.readBytes(status);
        paramMap.put("gpsstate", ByteUtil.byteArrayToInt(states, 1));
        paramMap.put("longitude", lls.doubleValue());
        paramMap.put("latitude", las.doubleValue());
        paramMap.put("speed", speeds);
        paramMap.put("gpsstatus", "");
        paramMap.put("nss", ByteUtil.byteArrayToInt(nss, 1));
        paramMap.put("ews", ByteUtil.byteArrayToInt(ews, 1));

    }

    /**
     * 检验和
     * 
     * @param msg
     * @param sign
     * @return
     */
    public static boolean checkSign(byte[] msg, byte[] sign) {
        byte[] msgSign = ByteUtil.sumCheck(msg, 1);
        if (ByteUtil.printHexString(msgSign).equals(ByteUtil.printHexString(sign))) {
            return false;
        }
        return true;
    }

    public static boolean checkSign2(byte[] msg, byte[] sign) {
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
        String temp = "EC010400989680B368";

        BigDecimal lls = new BigDecimal("11." + df.format(123));
        logger.debug(":::" + lls.doubleValue());

        byte[] temps = ByteUtil.hexString2Bytes(temp);

        try {
            Msg.conventMsg(temps, null, null);
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}
