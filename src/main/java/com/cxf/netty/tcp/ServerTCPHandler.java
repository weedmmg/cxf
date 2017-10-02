package com.cxf.netty.tcp;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.io.UnsupportedEncodingException;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.cxf.logger.Logs;
import com.cxf.netty.connection.Connection;
import com.cxf.netty.connection.ConnectionManager;
import com.cxf.netty.connection.NettyConnection;
import com.cxf.netty.tcp.convent.Msg;
import com.cxf.thread.UrlThreadFactory;
import com.cxf.util.PropertiesUtil;
import com.cxf.util.Strings;

@ChannelHandler.Sharable
public class ServerTCPHandler extends ChannelInboundHandlerAdapter {

    private final boolean security; // 是否启用加密
    private final ConnectionManager connectionManager;

    public ServerTCPHandler(boolean security, ConnectionManager connectionManager) {
        this.security = security;
        this.connectionManager = connectionManager;

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {

        // 更新心跳时间
        connectionManager.get(ctx.channel()).updateLastReadTime();

        // 处理数据
        byte[] data;
        try {
            data = Msg.conventMsg(msg, ctx, connectionManager);
            if (data.length > 1) {
                ctx.writeAndFlush(data);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        Connection connection = connectionManager.get(ctx.channel());
        Logs.CONN.error("client caught ex, conn={}", connection);
        Logs.CONN.error("caught an ex, channel={}, conn={}", ctx.channel(), connection, cause);
        ctx.close();
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        String channelId = ctx.channel().attr(NettyTCPServer.rcvChannel).get();
        if (!Strings.isBlank(channelId)) {
            Connection sendConnection = connectionManager.getById(channelId);
            if (sendConnection != null) {

                sendConnection.getChannel().attr(NettyTCPServer.sendChannel).set("");
                sendConnection.getChannel().writeAndFlush(new TextWebSocketFrame(ctx.channel().id() + " 长连接断开！"));
            }
        }

        Connection connection = connectionManager.removeAndClose(ctx.channel());

        String url = PropertiesUtil.getValue("system.logout.url");
        if (!Strings.isBlank(url)) {
            Map<String, Object> paramMap = new HashMap<String, Object>();
            paramMap.put("channelId", ctx.channel().id().toString());
            paramMap.put("serverIp", PropertiesUtil.getValue("netty.ip"));

            if (ctx != null) {
                ctx.executor().submit(new UrlThreadFactory(url, new JSONObject(paramMap).toString()));
            }
        }

        // EventBus.post(new ConnectionCloseEvent(connection));
        Logs.CONN.debug("client disconnected conn={}", connection);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Logs.CONN.debug("client connected conn={}", ctx.channel());
        Connection connection = new NettyConnection();
        connection.init(ctx.channel(), security);
        connectionManager.add(connection);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {

    }

    public static String getClientIp(SocketAddress address) {
        /**
         * 获取客户端IP
         */
        String ip = "";
        if (address != null) {
            ip = address.toString().trim();
            int index = ip.lastIndexOf(':');
            if (index < 1) {
                index = ip.length();
            }
            ip = ip.substring(1, index);
        }
        if (ip.length() > 15) {
            ip = ip.substring(Math.max(ip.indexOf("/") + 1, ip.length() - 15));
        }
        return ip;
    }
}
