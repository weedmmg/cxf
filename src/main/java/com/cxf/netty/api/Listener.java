package com.cxf.netty.api;

public interface Listener {

    void onSuccess(Object... args);

    void onFailure(Throwable cause);
}
