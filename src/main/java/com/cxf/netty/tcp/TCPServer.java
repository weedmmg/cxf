package com.cxf.netty.tcp;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;

import com.cxf.netty.ChannelType;
import com.cxf.netty.Server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

public class TCPServer implements Server {

    private final ChannelType channelType;
    private final InetSocketAddress localAddress;
    private Channel acceptorChannel;

    public static ChannelGroup allChannels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    public static ConcurrentHashMap<String, ChannelHandlerContext> allChannelMap = new ConcurrentHashMap<String, ChannelHandlerContext>();
    public static ConcurrentHashMap<String, ChannelHandlerContext> simpleAllChannelMap = new ConcurrentHashMap<String, ChannelHandlerContext>();

    // private ServerTCPHandler tcpHandler = new ServerTCPHandler();

    public TCPServer(ChannelType channelType, int port) {
        super();
        this.channelType = channelType;
        this.localAddress = new InetSocketAddress(port);
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {
        if (acceptorChannel != null)
            acceptorChannel.close().addListener(ChannelFutureListener.CLOSE);
    }

}
