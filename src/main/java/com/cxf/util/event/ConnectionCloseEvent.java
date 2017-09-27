package com.cxf.util.event;

import com.cxf.netty.connection.Connection;

public final class ConnectionCloseEvent implements Event {

    public final Connection connection;

    public ConnectionCloseEvent(Connection connection) {
        this.connection = connection;
    }
}
