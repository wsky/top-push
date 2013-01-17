package com.tmall.top.push.websocket;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketServlet;

import com.tmall.top.push.Client;
import com.tmall.top.push.PushManager;

public class FrontendServlet extends WebSocketServlet {

	private static final long serialVersionUID = -2545213842937092374L;

	@Override
	public WebSocket doWebSocketConnect(HttpServletRequest arg0, String arg1) {
		PushManager manager = PushManager.current();
		HashMap<String, String> headers = Utils.parseHeaders(arg0);
		Client client = manager.connectingClient(headers);

		WebSocketClientConnection clientConnection = Utils.getClientConnectionPool().acquire();
		clientConnection.init(client.getId(), headers, manager);

		return new FrontendWebSocket(manager, client, clientConnection);
	}

	private class FrontendWebSocket extends WebSocketBase {

		public FrontendWebSocket(PushManager manager, Client client,
				WebSocketClientConnection clientConnection) {
			super(manager, client, clientConnection);
		}

		@Override
		public void onMessage(String arg0) {
		}

	}

}
