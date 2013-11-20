package com.taobao.top.push.websocket;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.taobao.top.push.DefaultIdentity;

public class Utils {

	public static WebSocketClientConnectionPool pool;

	public static void initClientConnectionPool(int poolSize) {
		pool = new WebSocketClientConnectionPool(poolSize);
	}

	public static WebSocketClientConnectionPool getClientConnectionPool() {
		return pool;
	}

	public static HashMap<String, String> parseHeaders(
			HttpServletRequest request) {
		HashMap<String, String> headers = new HashMap<String, String>();
		Enumeration<String> names = request.getHeaderNames();
		while (names.hasMoreElements()) {
			String h = names.nextElement().toLowerCase();
			headers.put(h, request.getHeader(h));
		}
		return headers;
	}

	public static DefaultIdentity parseIdentity(Map<String, String> headers) throws Exception {
		String id = headers.get("origin");

		if (id == null || id.trim() == "")
			throw new Exception("origin is empty");

		return new DefaultIdentity(id);
	}
}
