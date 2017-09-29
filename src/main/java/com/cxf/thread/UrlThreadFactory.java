package com.cxf.thread;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cxf.netty.tcp.convent.Msg;
import com.cxf.util.Strings;

public class UrlThreadFactory implements Runnable {
	private static Logger logger = LoggerFactory.getLogger(Msg.class);
	private String url = "";
	private String json = "";

	public UrlThreadFactory(String url, String json) {
		this.url = url;
		this.json = json;
	}

	@Override
	public void run() {
		if (Strings.isBlank(url) || Strings.isBlank(json)) {
			logger.error("error url :" + url + " or json:" + json + " is null:");
			return;
		}
		try {
			logger.info("submit:" + url + json);
			HttpURLConnection conn = doOutputJson(initHttp(url + json, ""), "POST", "");

			conn.disconnect();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected HttpURLConnection initHttp(String url, String uri) throws Exception {
		URL localURL = new URL(url + uri);
		URLConnection connection = localURL.openConnection();
		HttpURLConnection httpURLConnection = (HttpURLConnection) connection;

		httpURLConnection.setDoOutput(true);
		httpURLConnection.setDoInput(true);
		return httpURLConnection;
	}

	protected HttpURLConnection doOutputJson(HttpURLConnection conn, String method, String json) throws Exception {
		conn.setDoOutput(true);
		conn.setRequestMethod(method);

		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		conn.setRequestProperty("Accept", "application/x-www-form-urlencoded");

		DataOutputStream out = new DataOutputStream(conn.getOutputStream());

		out.writeBytes(json);
		out.flush();
		out.close();
		return conn;
	}

}
