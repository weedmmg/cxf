package com.cxf.netty.tcp.convent;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import com.cxf.util.DateUtil;
import com.cxf.util.MD5Util;
import com.cxf.util.PropertiesUtil;
import com.cxf.util.Strings;

public class Msg {

    private static String encoding = "utf8";
    private static Logger logger = LoggerFactory.getLogger(Msg.class);
    private final static byte errorCmd = 0x04;
    private final static byte successCmd = 0x02;

    private static final String NUM_FORMAT = "00000";
    private static final String TIME_FORMAT = "00";
    private static final DecimalFormat df = new DecimalFormat(NUM_FORMAT);
    private static final DecimalFormat timeDf = new DecimalFormat(TIME_FORMAT);

    private static final String TIMEFORMAT = "yyyyMMddHHmmss";

    public static byte[] conventMsg(Object msg, ChannelHandlerContext ctx, ConnectionManager connectionManager) throws UnsupportedEncodingException {

        ByteBuf totalBytes = Unpooled.wrappedBuffer((byte[]) msg);
        ByteBuf cmdBytes = totalBytes.slice(0, 1), lenBytes = totalBytes.slice(1, 1);

        byte[] cmds = new byte[1], lens = new byte[1];

        cmdBytes.readBytes(cmds);
        lenBytes.readBytes(lens);

        int len = ByteUtil.byteArrayToInt(lens, 1), signLen = 1;

        ByteBuf dataBytes = totalBytes.slice(2, len);
        ByteBuf signBytes = totalBytes.slice(2 + len, signLen);
        ByteBuf checkBytes = totalBytes.slice(0, 2 + len);
        byte[] datas = new byte[len];
        dataBytes.readBytes(datas);
        byte[] checkBytesArray = new byte[2 + len], signBytesArray = new byte[signLen];

        checkBytes.readBytes(checkBytesArray);
        signBytes.readBytes(signBytesArray);

        if (checkSign(checkBytesArray, signBytesArray)) {
            logger.error("sign error data:" + ByteUtil.printHexString((byte[]) msg));

            return intMsg(errorCmd, ByteUtil.intToByteArray(2, 1));
        }
        handleMsg(cmds[0], datas, ctx, connectionManager, len);
        return intMsg(successCmd, ByteUtil.intToByteArray(1, 1));

    }

    public static void handleMsg(byte cmd, byte[] data, ChannelHandlerContext ctx, ConnectionManager connectionManager, int len) {

        logger.info("channelId:" + ctx.channel().id());
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

            ctx.channel().attr(NettyTCPServer.uid).set(uuid);
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
        case 0x08:
            ByteBuf dataBytes = Unpooled.wrappedBuffer(data),
            cmdBytes = dataBytes.slice(0, 1),
            stateBytes = dataBytes.slice(1, 1);
            byte[] cmds = new byte[1],
            states = new byte[1];
            stateBytes.readBytes(states);
            cmdBytes.readBytes(cmds);
            paramMap.put("code", "0");
            paramMap.put("msg", "channelId:" + ctx.channel().id() + "执行" + ByteUtil.byteArrayToInt(cmds, 1) + " 结果：" + ByteUtil.byteArrayToInt(states, 1));

            // Logs.WS.info("get info:" + new JSONObject(paramMap).toString());
            channelId = ctx.channel().attr(NettyTCPServer.rcvChannel).get();
            if (!Strings.isBlank(channelId)) {
                try {
                    Connection rcvConnection = connectionManager.getById(channelId);

                    if (rcvConnection != null) {
                        rcvConnection.send(new TextWebSocketFrame(new JSONObject(paramMap).toString()));
                        Logs.WS.info("send result msg:" + new JSONObject(paramMap).toString());
                    }

                    break;
                } catch (Exception e) {
                    Logs.WS.error("push result error:" + e.getMessage());
                }
            }
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
                    Logs.WS.error("push location error:" + e.getMessage());
                }
            }

            break;

        case (byte) 0xBC:

            Logs.WS.info("基站数据  info:" + new JSONObject(paramMap).toString());
            url = PropertiesUtil.getValue("system.jizhan.url");
            if (Strings.isBlank(url)) {
                logger.error("error system.jizhan.url is null:" + url);
                break;
            }

            if (ctx != null) {

                paramMap.put("channelId", ctx.channel().id().toString());
                paramMap.put("serverIp", PropertiesUtil.getValue("netty.ip"));
                initJizhanMap(paramMap, data);
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
                        Logs.WS.info("send jizhan msg:" + new JSONObject(paramMap).toString());
                    }

                    break;
                } catch (Exception e) {
                    Logs.WS.error("push jizhan error:" + e.getMessage());
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

    public static void initJizhanMap(Map<String, Object> paramMap, byte[] data) {
        paramMap.put("nettype", ByteUtil.byteArrayToInt(new byte[] { data[0], data[1] }, 2));
        paramMap.put("frerange", "");
        paramMap.put("basetime", getTime(data, 2));
        paramMap.put("timerange", ByteUtil.byteArrayToInt(new byte[] { data[9], data[10] }, 2));
        int count = ByteUtil.byteArrayToInt(new byte[] { data[11], data[12] }, 2);
        paramMap.put("equirange", count);

        List<Map> list = new ArrayList<Map>();
        for (int i = 0; i < count; i++) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("carid", getCarId(data, i * 13 + 13));
            try {
                map.put("timen", DateUtil.getUnixTimestap(DateUtil.StringToDate(getTime(data, i * 13 + 19), TIMEFORMAT)));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            list.add(map);
        }
        paramMap.put("equilist", list);
        // paramMap.put("3G", ByteUtil.byteArrayToInt(new byte[] { data[1] },
        // 1));
        // paramMap.put("4G", ByteUtil.byteArrayToInt(new byte[] { data[2] },
        // 1));
        // paramMap.put("170M", ByteUtil.byteArrayToInt(new byte[] { data[3] },
        // 1));
        // paramMap.put("315M", ByteUtil.byteArrayToInt(new byte[] { data[4] },
        // 1));
        // paramMap.put("433M", ByteUtil.byteArrayToInt(new byte[] { data[5] },
        // 1));
        // paramMap.put("868M", ByteUtil.byteArrayToInt(new byte[] { data[6] },
        // 1));
        // paramMap.put("915M/920M", ByteUtil.byteArrayToInt(new byte[] {
        // data[7] }, 1));

    }

    private static String getCarId(byte[] data, int pos) {

        String carid = ByteUtil.byteArrayToInt(new byte[] { data[pos], data[pos + 1], data[pos + 2], data[pos + 3], data[pos + 4], data[pos + 5] }, 6) + "";
        System.out.println(carid + "carid:");
        return carid;
    }

    private static String getTime(byte[] data, int pos) {
        String yearPre = timeDf.format(ByteUtil.byteArrayToInt(new byte[] { data[pos] }, 1)), year = timeDf.format(ByteUtil.byteArrayToInt(new byte[] { data[pos + 1] }, 1)), month = timeDf
                .format(ByteUtil.byteArrayToInt(new byte[] { data[pos + 2] }, 1)), day = timeDf.format(ByteUtil.byteArrayToInt(new byte[] { data[pos + 3] }, 1)), hh = timeDf.format(ByteUtil
                .byteArrayToInt(new byte[] { data[pos + 4] }, 1)), mm = timeDf.format(ByteUtil.byteArrayToInt(new byte[] { data[pos + 5] }, 1)), ss = timeDf.format(ByteUtil.byteArrayToInt(
                new byte[] { data[pos + 6] }, 1));

        return yearPre + year + month + day + hh + mm + ss;
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
        logger.error(ByteUtil.printHexString(msgSign) + "::" + ByteUtil.printHexString(sign));
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
        try {
            System.out.println(ByteUtil.printHexString("1".getBytes("utf8")));
        } catch (UnsupportedEncodingException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        String temp = "000114110a1b1016000005000110000001094614110a1b101f00".toUpperCase();

        byte[] temps = ByteUtil.hexString2Bytes(temp);

        try {
            Map<String, Object> map = new HashMap<String, Object>();
            initJizhanMap(map, temps);
            Logs.WS.info("send location msg:" + new JSONObject(map).toString());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}
