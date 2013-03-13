package com.taobao.top.push.websocket;

import java.util.HashMap;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketServlet;

import com.taobao.top.push.Client;
import com.taobao.top.push.Logger;
import com.taobao.top.push.LoggerFactory;
import com.taobao.top.push.PushManager;

public class FrontendServlet extends WebSocketServlet {

	private static final long serialVersionUID = -2545213842937092374L;

	private Logger logger;

	@Override
	public WebSocket doWebSocketConnect(HttpServletRequest arg0, String arg1) {
		PushManager manager = PushManager.current();
		this.logger = manager.getLoggerFactory().create(this);
		
		HashMap<String, String> headers = Utils.parseHeaders(arg0);
		this.dump(headers);
		
		Client client = manager.connectingClient(headers);

		WebSocketClientConnection clientConnection = Utils.getClientConnectionPool().acquire();
		clientConnection.init(client.getId(), headers, manager);

		return new FrontendWebSocket(
				manager.getLoggerFactory(), manager, client, clientConnection);
	}

	private void dump(HashMap<String, String> headers) {
		if (!this.logger.isDebugEnable())
			return;
		for (Entry<String, String> h : headers.entrySet()) {
			this.logger.debug("%s=%s", h.getKey(), h.getValue());
		}
	}

	private class FrontendWebSocket extends WebSocketBase {
		public FrontendWebSocket(LoggerFactory loggerFactory,
				PushManager manager, Client client, WebSocketClientConnection clientConnection) {
			super(loggerFactory, manager, client, clientConnection);
		}
	}

}
