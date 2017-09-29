package com.cxf.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class PropertiesUtil {

    static String propsPath = "/src/main/resources/";
    static String nettyProp = "netty.properties";

    public static Properties loadProperties() {
        return loadProperties(nettyProp);
    }

    public static Properties loadProperties(String fileName) {
        Properties properties = null;
        try {

            String path = System.getProperty("user.dir");

            FileInputStream in = new FileInputStream(path + propsPath + fileName);
            properties = new Properties();
            properties.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }

    public static String getValue(String fileName, String key) {
        Properties properties = loadProperties(fileName);
        String result = "";
        if (properties != null) {
            result = properties.getProperty(key);
        }
        return result;
    }

    public static String getValue(String key) {
        Properties properties = loadProperties();
        String result = "";
        if (properties != null) {
            result = properties.getProperty(key);
        }
        return result;
    }

    public static void main(String[] args) {

        long tickDuration = TimeUnit.SECONDS.toMillis(1);// 1s
                                                         // 每秒钟走一步，一个心跳周期内大致走一圈
        int ticksPerWheel = (int) (Integer.parseInt(PropertiesUtil.getValue("max.heartbeat")) / tickDuration);
        System.out.println(ticksPerWheel + "::" + tickDuration);
        Properties p = PropertiesUtil.loadProperties("netty.properties");
        System.out.print(p.get("netty.ip"));

    }
}
