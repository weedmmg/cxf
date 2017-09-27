package com.cxf.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.oio.OioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.oio.OioDatagramChannel;
import io.netty.channel.socket.oio.OioServerSocketChannel;

public class ServerBootstrapFactory {

    private ServerBootstrapFactory() {
    }

    public static ServerBootstrap createTCPServerBootstrap(final ChannelType channelType) throws UnsupportedOperationException {

        ServerBootstrap serverBootstrap = new ServerBootstrap();
        switch (channelType) {
        case NIO:
            EventLoopGroup bossGroup = new NioEventLoopGroup();
            EventLoopGroup workerGroup = new NioEventLoopGroup();
            serverBootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class);
            return serverBootstrap;
        case OIO:
            EventLoopGroup obossGroup = new OioEventLoopGroup();
            EventLoopGroup oworkerGroup = new OioEventLoopGroup();
            serverBootstrap.group(new OioEventLoopGroup());
            serverBootstrap.group(obossGroup, oworkerGroup).channel(OioServerSocketChannel.class);
            return serverBootstrap;
        default:
            throw new UnsupportedOperationException("Failed to create ServerBootstrap,  " + channelType + " not supported!");
        }
    }

    public static Bootstrap createUDPServerBootstrap(final ChannelType channelType) throws UnsupportedOperationException {

        Bootstrap serverBootstrap = new Bootstrap();
        switch (channelType) {
        case NIO:
            serverBootstrap.group(new NioEventLoopGroup());
            serverBootstrap.channel(NioDatagramChannel.class);
            return serverBootstrap;
        case OIO:
            serverBootstrap.group(new OioEventLoopGroup());
            serverBootstrap.channel(OioDatagramChannel.class);
            return serverBootstrap;
        default:
            throw new UnsupportedOperationException("Failed to create ServerBootstrap,  " + channelType + " not supported!");
        }
    }

}
