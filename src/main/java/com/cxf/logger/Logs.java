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

package com.cxf.logger;

import org.apache.log4j.Logger;

/**
 * Created by ohun on 2016/5/16.
 * 
 * @author ohun@live.cn
 */
public class Logs {

    static String propsPath = "/src/main/resources/";

    public static void init() {
        String path = System.getProperty("user.dir");
        // System.setProperty("log.home", path + "logs");
        // System.setProperty("log.root.level", "warn");

        System.setProperty("log4j.configurationFile", path + propsPath + "log4j.properties");
    }

    public final static Logger Console = Logger.getLogger("console"),

    CONN = Logger.getLogger("cxf.conn.log"),

    PUSH = Logger.getLogger("cxf.push.log"),

    HTTP = Logger.getLogger("cxf.http.log"),

    TCP = Logger.getLogger("cxf.tcp.log"),

    HB = Logger.getLogger("cxf.hb.log");

}
