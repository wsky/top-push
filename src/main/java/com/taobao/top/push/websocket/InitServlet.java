package com.taobao.top.push.websocket;

import java.util.HashMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import com.taobao.top.push.Client;
import com.taobao.top.push.ClientConnection;
import com.taobao.top.push.ClientStateHandler;
import com.taobao.top.push.DefaultLoggerFactory;
import com.taobao.top.push.PushManager;
import com.taobao.top.push.UnauthorizedException;

public class InitServlet extends HttpServlet {

	private static final long serialVersionUID = 3059398081890461730L;

	@Override
	public void init(ServletConfig config) throws ServletException {
		Utils.initClientConnectionPool(100000);
		PushManager.current(new PushManager(
				new DefaultLoggerFactory(),
				this.get(config, "maxConnectionCount"),
				this.get(config, "maxMessageSize"),
				this.get(config, "maxMessageBufferCount"),
				this.get(config, "senderCount"),
				this.get(config, "senderIdle"),
				this.get(config, "stateBuilderIdle")));
		PushManager.current().setClientStateHandler(new ClientStateHandler() {

			@Override
			public void onClientPending(Client client) {
			}

			@Override
			public void onClientOffline(Client client) {
			}

			@Override
			public void onClientIdle(Client client) {
			}

			@Override
			public void onClientConnect(Client client, ClientConnection clientConnection) throws UnauthorizedException {
			}

			@Override
			public void onClientDisconnect(Client client, ClientConnection clientConnection) {
			}

			@Override
			public String onClientConnecting(HashMap<String, String> headers) {
				return headers.get("origin");
			}
		});

		super.init(config);
	}

	private int get(ServletConfig config, String k) {
		return Integer.parseInt(config.getInitParameter(k));
	}
}
