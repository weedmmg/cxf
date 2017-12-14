package com.cxf.netty.websocket;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cxf.logger.Logs;
import com.cxf.netty.connection.Connection;
import com.cxf.netty.connection.ConnectionManager;
import com.cxf.netty.connection.NettyConnection;
import com.cxf.netty.tcp.NettyTCPServer;
import com.cxf.netty.tcp.convent.Msg;
import com.cxf.util.ByteUtil;
import com.cxf.util.Strings;
import com.cxf.util.event.ConnectionCloseEvent;
import com.cxf.util.event.EventBus;

/**
 * Echoes uppercase content of text frames.
 */
@ChannelHandler.Sharable
public class WebSocketChannelHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    private final ConnectionManager connectionManager;

    Map<String, Object> resultMap = new HashMap<String, Object>();

    public WebSocketChannelHandler(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {
        if (frame instanceof TextWebSocketFrame) {

            String message = frame.content().toString(io.netty.util.CharsetUtil.UTF_8);

            Map<String, Object> paramMap = JSON.parseObject(message);

            String channelId = String.valueOf(paramMap.get("channelId")), times = String.valueOf(paramMap.get("times")), cmdstr = String.valueOf(paramMap.get("cmd")), sign = String.valueOf(paramMap
                    .get("sign")), data = String.valueOf(paramMap.get("data"));

            resultMap.put("code", "0");
            resultMap.put("msg", "执行成功");

            if (Strings.isBlank(channelId)) {
                Logs.WS.error("error channelId:" + channelId + " is null.");
                ctx.writeAndFlush(new TextWebSocketFrame(new JSONObject(resultMap).toString()));
                return;
            }

            if (!Strings.isBlank(times)) {
                // 处理调整频率代码
                ctx.channel().attr(NettyTCPServer.sendChannel).set(channelId);

                Connection sendConnection = connectionManager.getById(channelId);
                if (sendConnection != null) {
                    sendConnection.getChannel().attr(NettyTCPServer.rcvChannel).set(ctx.channel().id().toString());
                    Logs.WS.info("set receve id:" + ctx.channel().id().toString());
                    byte cmd = 0x03;
                    try {
                        byte[] pushMsg = Msg.intMsg(cmd, ByteUtil.intToByteArray(Integer.valueOf(times), 2));
                        sendConnection.send(pushMsg);

                    } catch (Exception e) {
                        e.printStackTrace();
                        Logs.WS.error("push modify time error:" + e.getMessage());
                    }
                }
            }

            if (!Strings.isBlank(cmdstr)) {
                // 处理调整频率代码
                ctx.channel().attr(NettyTCPServer.sendChannel).set(channelId);

                Connection sendConnection = connectionManager.getById(channelId);
                if (sendConnection != null) {
                    sendConnection.getChannel().attr(NettyTCPServer.rcvChannel).set(ctx.channel().id().toString());
                    Logs.WS.info("set receve id:" + ctx.channel().id().toString());
                    byte cmd = 0x09;
                    try {
                        int cmdInt = Integer.valueOf(cmdstr), dataInt = Integer.valueOf(data);
                        byte[] pushMsg = Msg.intMsg(cmd, ByteUtil.byteMerger(ByteUtil.intToByteArray(cmdInt, 1), ByteUtil.intToByteArray(dataInt, 2)));
                        sendConnection.send(pushMsg);

                    } catch (Exception e) {
                        e.printStackTrace();
                        Logs.WS.error("push modify time error:" + e.getMessage());
                    }
                }
            }

            if (!Strings.isBlank(sign)) {
                // 处理自定义代码
                ctx.channel().attr(NettyTCPServer.sendChannel).set(channelId);

                Connection sendConnection = connectionManager.getById(channelId);
                if (sendConnection != null) {
                    sendConnection.getChannel().attr(NettyTCPServer.rcvChannel).set(ctx.channel().id().toString());
                    Logs.WS.info("set receve id:" + ctx.channel().id().toString());
                    byte cmd = 0x06;
                    try {
                        int signInt = Integer.valueOf(sign), dataInt = Integer.valueOf(data);
                        byte[] pushMsg = Msg.intMsg(cmd, ByteUtil.byteMerger(ByteUtil.intToByteArray(signInt, 1), ByteUtil.intToByteArray(dataInt, 4)));
                        sendConnection.send(pushMsg);

                    } catch (Exception e) {
                        e.printStackTrace();
                        Logs.WS.error("push sign error:" + e.getMessage());
                    }
                }
            }

            ctx.writeAndFlush(new TextWebSocketFrame(new JSONObject(resultMap).toString()));
            Logs.WS.debug("msg:" + message);
            // LOGGER.debug("channelRead conn={}, packet={}", ctx.channel(),
            // connection.getSessionContext(), packet);
            // receiver.onReceive(packet, connection);
        } else {
            Logs.WS.error("unsupported frame type: " + frame.getClass().getName());

        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        Connection connection = connectionManager.get(ctx.channel());
        Logs.CONN.debug("client caught ex, conn={}", connection);

        ctx.close();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Logs.CONN.info("client connected conn={}", ctx.channel());
        Connection connection = new NettyConnection();
        connection.init(ctx.channel(), false);
        connectionManager.add(connection);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {

        String channelId = ctx.channel().attr(NettyTCPServer.sendChannel).get();
        if (!Strings.isBlank(channelId)) {
            Connection sendConnection = connectionManager.getById(channelId);
            if (sendConnection != null) {

                sendConnection.getChannel().attr(NettyTCPServer.rcvChannel).set("");
            }
        }
        Connection connection = connectionManager.removeAndClose(ctx.channel());
        EventBus.post(new ConnectionCloseEvent(connection));
        Logs.CONN.info("client disconnected conn={}", connection);
    }
}
