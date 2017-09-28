package com.cxf.netty.tcp;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.net.SocketAddress;

import com.cxf.logger.Logs;
import com.cxf.netty.connection.Connection;
import com.cxf.netty.connection.ConnectionManager;
import com.cxf.netty.connection.NettyConnection;
import com.cxf.util.event.ConnectionCloseEvent;
import com.cxf.util.event.EventBus;

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

        System.out.println(msg);
        // ctx.writeAndFlush("213123123");
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

    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Logs.CONN.info("client connected conn={}", ctx.channel());
        Connection connection = new NettyConnection();
        connection.init(ctx.channel(), security);
        connectionManager.add(connection);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Connection connection = connectionManager.removeAndClose(ctx.channel());
        //EventBus.post(new ConnectionCloseEvent(connection));
        Logs.CONN.info("client disconnected conn={}", connection);
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
