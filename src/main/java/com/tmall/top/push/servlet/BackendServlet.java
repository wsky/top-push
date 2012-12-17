package com.tmall.top.push.servlet;

import java.util.Hashtable;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketServlet;
import org.eclipse.jetty.websocket.WebSocket.FrameConnection;
import org.eclipse.jetty.websocket.WebSocket.OnTextMessage;

import com.tmall.top.push.Client;
import com.tmall.top.push.ClientManager;
import com.tmall.top.push.WebSocketClientConnection;
import com.tmall.top.push.messaging.ConfirmMessage;
import com.tmall.top.push.messaging.EventMessage;
import com.tmall.top.push.messaging.Message;

public class BackendServlet extends WebSocketServlet {

	@Override
	public WebSocket doWebSocketConnect(HttpServletRequest arg0, String arg1) {
		Hashtable<String, String> headers = new Hashtable<String, String>();
		headers.put("id", "confirm");
		ClientManager manager = ClientManager.Current();
		WebSocketClientConnection clientConnection = manager.ClientConnectionPool.Acquire();
		clientConnection.init(headers);
		
		return new BackendWebSocket(
				manager.getClient(clientConnection.getId()), clientConnection);
	}

	public class BackendWebSocket implements OnTextMessage{

		private Client client;
		private WebSocketClientConnection clientConnection;

		public BackendWebSocket(Client client,
				WebSocketClientConnection clientConnection) {
			this.client = client;
			this.clientConnection = clientConnection;
		}
		
		@Override
		public void onClose(int arg0, String arg1) {
			this.release();
		}

		@Override
		public void onOpen(Connection arg0) {
			this.clientConnection.init(arg0);
			this.client.AddConnection(this.clientConnection);
		}

		@Override
		public void onMessage(String arg0) {
			Message msg = this.clientConnection.parse(arg0);
			if (msg instanceof EventMessage)
				ClientManager.Current().Events.add((EventMessage) msg);
		}
		
		private void release() {
			this.client.RemoveConnection(this.clientConnection);
			this.clientConnection.clear();
			ClientManager.Current().ClientConnectionPool.Release(this.clientConnection);
		}
		
	}
}
