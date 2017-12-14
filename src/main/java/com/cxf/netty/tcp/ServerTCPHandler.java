package com.cxf.netty.tcp;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import java.io.UnsupportedEncodingException;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import com.alibaba.fastjson.JSONObject;
import com.cxf.logger.Logs;
import com.cxf.netty.connection.Connection;
import com.cxf.netty.connection.ConnectionManager;
import com.cxf.netty.connection.NettyConnection;
import com.cxf.netty.tcp.convent.Msg;
import com.cxf.thread.NamedThreadFactory;
import com.cxf.thread.ThreadNames;
import com.cxf.thread.UrlThreadFactory;
import com.cxf.util.PropertiesUtil;
import com.cxf.util.Strings;

@ChannelHandler.Sharable
public class ServerTCPHandler extends ChannelInboundHandlerAdapter {

  private final boolean security; // 是否启用加密
  private final ConnectionManager connectionManager;
  private HashedWheelTimer timer;
  private final ConnectionHolderFactory holderFactory;

  public ServerTCPHandler(boolean security, ConnectionManager connectionManager) {
    this.security = security;
    this.connectionManager = connectionManager;
    this.holderFactory = HeartbeatCheckTask::new;
    long tickDuration =
        TimeUnit.SECONDS.toMillis(Integer.parseInt(PropertiesUtil.getValue("tickduration")));// 1s
    // 每秒钟走一步，一个心跳周期内大致走一圈

    int ticksPerWheel =
        (int) (Integer.parseInt(PropertiesUtil.getValue("max.heartbeat")) / tickDuration);
    this.timer =
        new HashedWheelTimer(new NamedThreadFactory(ThreadNames.T_CONN_TIMER), tickDuration,
            TimeUnit.MILLISECONDS, ticksPerWheel);
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
    // Logs.CONN.error("client caught ex, conn={}", connection);
    Logs.CONN.debug("caught an ex, channel={}, conn={} exception={}", ctx.channel(), connection,
        cause);
    ctx.close();
  }

  @Override
  public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
    String channelId = ctx.channel().attr(NettyTCPServer.rcvChannel).get();
    if (!Strings.isBlank(channelId)) {
      Connection sendConnection = connectionManager.getById(channelId);
      if (sendConnection != null) {

        sendConnection.getChannel().attr(NettyTCPServer.sendChannel).set("");
        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("code", "-1");
        resultMap.put("msg", ctx.channel().id() + " 长连接断开！");

        sendConnection.getChannel().writeAndFlush(
            new TextWebSocketFrame(new JSONObject(resultMap).toString()));
      }
    }

    Connection connection = connectionManager.removeAndClose(ctx.channel());

    String url = PropertiesUtil.getValue("system.logout.url");
    String uuid = ctx.channel().attr(NettyTCPServer.uid).get();
    if ((!Strings.isBlank(url)) && (!Strings.isBlank(uuid))) {
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

    holderFactory.create(connection);


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

  private class HeartbeatCheckTask implements ConnectionHolder, TimerTask {

    private byte timeoutTimes = 0;
    private Connection connection;

    private HeartbeatCheckTask(Connection connection) {
      this.connection = connection;
      this.startTimeout();
    }

    void startTimeout() {
      Connection connection = this.connection;

      if (connection != null && connection.isConnected()) {

        timer.newTimeout(this, 0, TimeUnit.MILLISECONDS);
      }
    }

    @Override
    public void run(Timeout timeout) throws Exception {
      Connection connection = this.connection;

      if (connection == null || !connection.isConnected()) {

        Logs.HB.info("heartbeat timeout times={}, connection disconnected, conn={}", timeoutTimes,
            connection);
        return;
      }

      if (connection.isReadTimeout()) {
        if (++timeoutTimes > Integer.valueOf((PropertiesUtil.getValue("max-hb-timeout-times")))) {
          connection.close();
          Logs.HB.warn("client heartbeat timeout times={}, do close conn={}", timeoutTimes,
              connection);
          return;
        } else {
          Logs.HB
              .info("client heartbeat timeout times={}, connection={}", timeoutTimes, connection);
        }
      } else {
        timeoutTimes = 0;
      }
      startTimeout();
    }

    @Override
    public void close() {
      if (connection != null) {
        connection.close();
        connection = null;
      }
    }

    @Override
    public Connection get() {
      return connection;
    }

  }

  private interface ConnectionHolder {

    Connection get();

    void close();
  }

  @FunctionalInterface
  private interface ConnectionHolderFactory {

    ConnectionHolder create(Connection connection);
  }
}
