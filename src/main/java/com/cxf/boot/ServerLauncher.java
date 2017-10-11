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

package com.cxf.boot;

import com.cxf.netty.connection.ConnectionManager;
import com.cxf.netty.connection.ServerConnectionManager;
import com.cxf.netty.core.ConnectionServer;
import com.cxf.netty.websocket.WebsocketServer;

public final class ServerLauncher {

    ConnectionServer server;
    WebsocketServer wsServer;
    private final ConnectionManager connectionManager = new ServerConnectionManager(true);;

    public void init() {
        server = new ConnectionServer(connectionManager);
        wsServer = new WebsocketServer(connectionManager);
        server.init();
        wsServer.init();

    }

    public void start() {
        // chain.start();

        wsServer.start();
        server.start();
    }

    public void stop() {
        // chain.stop();
        server.stop();
        wsServer.stop();
    }

}
