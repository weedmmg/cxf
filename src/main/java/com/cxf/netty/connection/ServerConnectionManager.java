/*
 * (C) Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *   ohun@live.cn (夜色)
 */

package com.cxf.netty.connection;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import com.cxf.logger.Logs;
import com.cxf.thread.NamedThreadFactory;
import com.cxf.thread.ThreadNames;
import com.cxf.util.PropertiesUtil;

import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;

/**
 * Created by ohun on 2015/12/22.
 *
 * @author ohun@live.cn
 */
public final class ServerConnectionManager implements ConnectionManager {
	private final ConcurrentMap<ChannelId, ConnectionHolder> connections = new ConcurrentHashMap<>();
	private final ConnectionHolder DEFAULT = new SimpleConnectionHolder(null);
	private final boolean heartbeatCheck;
	private final ConnectionHolderFactory holderFactory;
	private HashedWheelTimer timer;

	public ServerConnectionManager(boolean heartbeatCheck) {
		this.heartbeatCheck = heartbeatCheck;
		this.holderFactory = heartbeatCheck ? HeartbeatCheckTask::new : SimpleConnectionHolder::new;
	}

	@Override
	public void init() {
		if (heartbeatCheck) {
			long tickDuration = TimeUnit.SECONDS.toMillis(Integer.parseInt(PropertiesUtil.getValue("tickduration")));// 1s
																														// 每秒钟走一步，一个心跳周期内大致走一圈

			int ticksPerWheel = (int) (Integer.parseInt(PropertiesUtil.getValue("max.heartbeat")) / tickDuration);
			this.timer = new HashedWheelTimer(new NamedThreadFactory(ThreadNames.T_CONN_TIMER), tickDuration,
					TimeUnit.MILLISECONDS, ticksPerWheel);
		}
	}

	@Override
	public void destroy() {
		if (timer != null) {
			timer.stop();
		}
		connections.values().forEach(ConnectionHolder::close);
		connections.clear();
	}

	@Override
	public Connection get(Channel channel) {
		return connections.getOrDefault(channel.id(), DEFAULT).get();
	}

	@Override
	public void add(Connection connection) {
		connections.putIfAbsent(connection.getChannel().id(), holderFactory.create(connection));
	}

	@Override
	public Connection removeAndClose(Channel channel) {
		ConnectionHolder holder = connections.remove(channel.id());
		if (holder != null) {
			Connection connection = holder.get();
			holder.close();
			return connection;
		}

		// add default
		Connection connection = new NettyConnection();
		connection.init(channel, false);
		connection.close();
		return connection;
	}

	@Override
	public int getConnNum() {
		return connections.size();
	}

	private interface ConnectionHolder {
		Connection get();

		void close();
	}

	private static class SimpleConnectionHolder implements ConnectionHolder {
		private final Connection connection;

		private SimpleConnectionHolder(Connection connection) {
			this.connection = connection;
		}

		@Override
		public Connection get() {
			return connection;
		}

		@Override
		public void close() {
			if (connection != null) {
				connection.close();
			}
		}
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
				Logs.HB.info("heartbeat timeout times={}, connection disconnected, conn={}", timeoutTimes, connection);
				return;
			}

			if (connection.isReadTimeout()) {
				if (++timeoutTimes > Integer.valueOf((PropertiesUtil.getValue("max-hb-timeout-times")))) {
					connection.close();
					Logs.HB.warn("client heartbeat timeout times={}, do close conn={}", timeoutTimes, connection);
					return;
				} else {
					Logs.HB.info("client heartbeat timeout times={}, connection={}", timeoutTimes, connection);
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

	@FunctionalInterface
	private interface ConnectionHolderFactory {
		ConnectionHolder create(Connection connection);
	}
}
