package com.cxf.netty;

/**
 * netty server
 * 
 * @author cxf
 * @company hrcf
 * @date Sep 27, 2017 9:30:42 PM
 */
public interface Server {

    /**
     * start server
     */
    public void start();

    /**
     * stop server
     */
    public void stop();

}
