package com.cxf.netty.tcp;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;

import java.net.InetSocketAddress;

import com.cxf.logger.Logs;
import com.cxf.netty.ChannelType;
import com.cxf.netty.ServerBootstrapFactory;

final class ServerTCPChannelFactory {

    protected static Channel createAcceptorChannel(final ChannelType channelType, final InetSocketAddress localAddress, final ServerTCPHandler serverHandler) throws InterruptedException {
        final ServerBootstrap serverBootstrap = ServerBootstrapFactory.createTCPServerBootstrap(channelType);
        EventLoopGroup group = new NioEventLoopGroup();
        serverBootstrap.option(ChannelOption.SO_REUSEADDR, false).handler(new LoggingHandler(LogLevel.INFO)).childHandler(new ChannelInitializer<SocketChannel>() {

            @Override
            protected void initChannel(final SocketChannel ch) throws Exception {
                final ChannelPipeline pipeline = ch.pipeline();

                pipeline.addLast("readTimeoutHandler", new ReadTimeoutHandler(3000));
                pipeline.addLast("handler", serverHandler);
            }
        });
        try {

            serverBootstrap.group(group);
            ChannelFuture channelFuture = serverBootstrap.bind(new InetSocketAddress(localAddress.getPort())).sync();
            channelFuture.awaitUninterruptibly();
            if (channelFuture.isSuccess()) {
                channelFuture.channel().closeFuture().sync();
                return channelFuture.channel();
            } else {

            }
        } catch (InterruptedException e) {
            Logs.Console.error("Netty Server Create Exception:" + e.getMessage());
        } finally {
            group.shutdownGracefully().sync();// 关闭EventLoopGroup，释放掉所有资源包括创建的线程
        }
        return null;
    }
}
