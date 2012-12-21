package com.tmall.top.push.websocket;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import com.tmall.top.push.PushManager;

public class InitServlet extends HttpServlet {

	private static final long serialVersionUID = 3059398081890461730L;

	@Override
	public void init(ServletConfig config) throws ServletException {
		Utils.initClientConnectionPool(100000);
		PushManager.current(new PushManager(50000, 1024, 1024, 100000, 100000,
				4, 2000, 1000));
		super.init(config);
	}
}
