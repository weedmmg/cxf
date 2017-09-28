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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ohun on 2016/5/16.
 * 
 * @author ohun@live.cn
 */
public class Logs {

	public static void init() {
		System.setProperty("logback.configurationFile", "log4j.properties");
	}

	public final static Logger Console = LoggerFactory.getLogger("console"),

	CONN = LoggerFactory.getLogger("cxf.conn.log"),

	PUSH = LoggerFactory.getLogger("cxf.push.log"),

	HTTP = LoggerFactory.getLogger("cxf.http.log"),

	TCP = LoggerFactory.getLogger("cxf.tcp.log"),
	
	HB = LoggerFactory.getLogger("cxf.hb.log");
	

}
