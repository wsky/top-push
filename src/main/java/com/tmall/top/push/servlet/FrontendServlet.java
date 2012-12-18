package com.tmall.top.push.servlet;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketServlet;

import com.tmall.top.push.Client;
import com.tmall.top.push.PushManager;
import com.tmall.top.push.Receiver;
import com.tmall.top.push.WebSocketClientConnection;

//handle all client's request
public class FrontendServlet extends WebSocketServlet {

	@Override
	public WebSocket doWebSocketConnect(HttpServletRequest arg0, String arg1) {
		PushManager manager = PushManager.Current();
		WebSocketClientConnection clientConnection = manager.getClientConnectionPool()
				.acquire();
		clientConnection.init(Utils.parseHeaders(arg0));

		return new FrontendWebSocket(manager.getReceiver(),
				manager.getClient(clientConnection.getId()), clientConnection);
	}

	private class FrontendWebSocket extends WebSocketBase {

		public FrontendWebSocket(Receiver receiver, Client client,
				WebSocketClientConnection clientConnection) {
			super(receiver, client, clientConnection);
		}
	}

}
