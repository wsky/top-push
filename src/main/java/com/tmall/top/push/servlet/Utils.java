package com.tmall.top.push.servlet;

import java.util.Enumeration;
import java.util.Hashtable;

import javax.servlet.http.HttpServletRequest;

public class Utils {
	public static Hashtable<String, String> parseHeaders(
			HttpServletRequest request) {
		Hashtable<String, String> headers = new Hashtable<String, String>();
		Enumeration<String> names = request.getHeaderNames();
		while (names.hasMoreElements()) {
			String h = names.nextElement();
			headers.put(h, request.getHeader(h));
			System.out.println(String.format("%s=%s", h, request.getHeader(h)));
		}
		return headers;
	}
}
