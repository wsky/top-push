package com.tmall.top.websocket;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketServlet;

import com.tmall.top.push.Client;
import com.tmall.top.push.PushManager;

//handle all client's request
public class FrontendServlet extends WebSocketServlet {

	@Override
	public WebSocket doWebSocketConnect(HttpServletRequest arg0, String arg1) {
		PushManager manager = PushManager.current();

		WebSocketClientConnection clientConnection = Utils
				.getClientConnectionPool().acquire();
		clientConnection.init(Utils.parseHeaders(arg0), manager);

		return new FrontendWebSocket(
				manager.getClient(clientConnection.getId()), clientConnection);
	}

	private class FrontendWebSocket extends WebSocketBase {

		public FrontendWebSocket(Client client,
				WebSocketClientConnection clientConnection) {
			super(client, clientConnection);
		}
	}

}
