package com.tmall.top.push.websocket;

import java.util.Enumeration;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.fastjson.JSON;
import com.tmall.top.push.PushManager;

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

	// implement an easy RPC for publisher
	public static Response processRequest(String message, PushManager manager) {
		Request request = JSON.parseObject(message, Request.class);
		Response response = null;
		Exception exception = null;

		if (request.Target.equalsIgnoreCase("isOnline")) {
			response = new Response();
			try {
				response.Result = JSON.toJSONString(manager
						.isOnlineClient(request.Arguments.get("id")));
			} catch (Exception e) {
				e.printStackTrace();
				exception = e;
			}
		}
		if (response == null) {
			response = new Response();
			response.IsError = true;
			response.ErrorPhrase = "invalid request:" + request.Target;
		}
		if (exception != null) {
			response.IsError = true;
			response.ErrorPhrase = exception.getMessage();
		}
		return response;
	}
}
