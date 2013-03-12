package com.taobao.top.push.websocket;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketServlet;

import com.taobao.top.push.Client;
import com.taobao.top.push.PushManager;

public class BackendServlet extends WebSocketServlet {

	private static final long serialVersionUID = 3431855312865710986L;

	@Override
	public WebSocket doWebSocketConnect(HttpServletRequest arg0, String arg1) {
		PushManager manager = PushManager.current();
		HashMap<String, String> headers = Utils.parseHeaders(arg0);
		Client client = manager.connectingClient(headers);

		WebSocketClientConnection clientConnection = Utils.getClientConnectionPool().acquire();
		clientConnection.init(client.getId(), headers, manager);

		return new BackendWebSocket(manager, client, clientConnection);
	}

	public class BackendWebSocket extends WebSocketBase {

		public BackendWebSocket(PushManager manager, 
				Client client,
				WebSocketClientConnection clientConnection) {
			super(manager, client, clientConnection);
		}

		@Override
		public void onMessage(String arg0) {
		}
	}
}
