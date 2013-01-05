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
		PushManager.current(new PushManager(
				this.get(config, "maxConnectionCount"),
				this.get(config, "maxMessageSize"),
				this.get(config, "maxMessageBufferCount"),
				this.get(config, "senderCount"),
				this.get(config, "senderIdle"),
				this.get(config, "stateBuilderIdle")));
		super.init(config);
	}

	private int get(ServletConfig config, String k) {
		return Integer.parseInt(config.getInitParameter(k));
	}
}
