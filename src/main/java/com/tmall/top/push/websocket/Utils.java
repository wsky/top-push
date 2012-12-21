package com.tmall.top.push.websocket;

import java.util.Enumeration;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

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
			System.out.println(String.format("%s=%s", h, request.getHeader(h)));
		}
		return headers;
	}
}
