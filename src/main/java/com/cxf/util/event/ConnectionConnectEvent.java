package com.cxf.util.event;

import com.cxf.netty.connection.Connection;

public final class ConnectionConnectEvent implements Event {

    public final Connection connection;

    public ConnectionConnectEvent(Connection connection) {
        this.connection = connection;
    }
}
