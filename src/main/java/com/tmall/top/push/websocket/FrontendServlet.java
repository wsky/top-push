package com.tmall.top.push.websocket;

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

		WebSocketClientConnection clientConnection = Utils
				.getClientConnectionPool().acquire();
		clientConnection.init(Utils.parseHeaders(arg0), manager);

		return new FrontendWebSocket(manager,
				manager.getClient(clientConnection.getId()), clientConnection);
	}

	private class FrontendWebSocket extends WebSocketBase {

		public FrontendWebSocket(PushManager manager, Client client,
				WebSocketClientConnection clientConnection) {
			super(manager, client, clientConnection);
		}

	}

}
