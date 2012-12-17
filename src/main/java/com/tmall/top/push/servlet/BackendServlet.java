package com.tmall.top.push.servlet;

import java.util.Hashtable;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketServlet;

import com.tmall.top.push.Client;
import com.tmall.top.push.PushManager;
import com.tmall.top.push.Receiver;
import com.tmall.top.push.WebSocketClientConnection;

public class BackendServlet extends WebSocketServlet {

	@Override
	public WebSocket doWebSocketConnect(HttpServletRequest arg0, String arg1) {
		//headers.put("id", "confirm");
		PushManager manager = PushManager.Current();
		WebSocketClientConnection clientConnection = manager.ClientConnectionPool
				.acquire();
		clientConnection.init(Utils.parseHeaders(arg0));

		return new BackendWebSocket(manager.receiver,
				manager.getClient(clientConnection.getId()), clientConnection);
	}

	public class BackendWebSocket extends WebSocketBase {
		public BackendWebSocket(Receiver receiver, Client client,
				WebSocketClientConnection clientConnection) {
			super(receiver, client, clientConnection);
		}

	}
}
