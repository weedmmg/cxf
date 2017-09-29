/**
 * Project Name:kyle
 * File Name:Main.java
 * Package Name:com.cxf.boot
 * Date:2017年9月27日下午2:32:39
 * Copyright (c) 2017, bluemobi All Rights Reserved.
 *
 */

package com.cxf.boot;

import com.cxf.logger.Logs;

/**
 * Description: <br/>
 * Date: 2017年9月27日 下午2:32:39 <br/>
 * 
 * @author cxf
 * @version
 * @see
 */
public class Main {

    /**
     * 源码启动请不要直接运行此方法，否则不能正确加载配置文件
     * 
     * @param args
     *            启动参数
     */
    public static void main(String[] args) {
        Logs.init();
        Logs.Console.info("launch netty server...");
        final ServerLauncher launcher = new ServerLauncher();
        launcher.init();
        launcher.start();
        addHook(launcher);
    }

    /**
     * 注意点 1.不要ShutdownHook Thread 里调用System.exit()方法，否则会造成死循环。
     * 2.如果有非守护线程，只有所有的非守护线程都结束了才会执行hook 3.Thread默认都是非守护线程，创建的时候要注意
     * 4.注意线程抛出的异常，如果没有被捕获都会跑到Thread.dispatchUncaughtException
     * 
     * @param launcher
     */
    private static void addHook(final ServerLauncher launcher) {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() { // 一般做法

                    public void run() {
                        try {
                            launcher.stop();
                        } catch (Exception e) {
                            Logs.Console.error("server stop ex", e);
                        }
                        Logs.Console.info("jvm exit, all service stopped.");
                    }
                })

        );
    }

}
