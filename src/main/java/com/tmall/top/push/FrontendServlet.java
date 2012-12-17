package com.tmall.top.push;

import java.util.Enumeration;
import java.util.Hashtable;
import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketServlet;

import com.tmall.top.push.messaging.ConfirmMessage;
import com.tmall.top.push.messaging.Message;

//handle all client's request
public class FrontendServlet extends WebSocketServlet {

	@Override
	public WebSocket doWebSocketConnect(HttpServletRequest request, String arg) {
		// parse HTTP headers
		Hashtable<String, String> headers = new Hashtable<String, String>();
		Enumeration<String> names = request.getHeaderNames();
		while (names.hasMoreElements()) {
			String h = names.nextElement();
			headers.put(h, request.getHeader(h));
			System.out.println(String.format("%s=%s", h, request.getHeader(h)));
		}

		ClientManager manager = ClientManager.Current();
		WebSocketClientConnection clientConnection = manager.ClientConnectionPool.Acquire();
		clientConnection.init(headers);
		
		return new FrontendWebSocket(
				manager.getClient(clientConnection.getId()), clientConnection);
	}

	private class FrontendWebSocket implements WebSocket.OnTextMessage,
			WebSocket.OnFrame, WebSocket.OnControl {

		private FrameConnection frameConnection;
		private Client client;
		private WebSocketClientConnection clientConnection;

		public FrontendWebSocket(Client client,
				WebSocketClientConnection clientConnection) {
			this.client = client;
			this.clientConnection = clientConnection;
		}

		@Override
		public void onHandshake(FrameConnection arg0) {
			int statusCode = this.clientConnection.verifyHeaders();
			if (statusCode != 101) {
				arg0.close(statusCode, "invalid header");
				this.release();
				return;
			}
			this.frameConnection = arg0;
			this.clientConnection.init(this.frameConnection);
			this.client.AddConnection(this.clientConnection);
		}

		@Override
		public void onOpen(Connection arg0) {
			this.clientConnection.init(arg0);
		}

		@Override
		public void onClose(int arg0, String arg1) {
			this.release();
		}

		@Override
		public boolean onControl(byte arg0, byte[] arg1, int arg2, int arg3) {
			if (this.frameConnection.isPing(arg0)) {
				this.clientConnection.ReceivePing();
			}
			return false;
		}

		@Override
		public boolean onFrame(byte arg0, byte arg1, byte[] arg2, int arg3,
				int arg4) {
			return false;
		}

		@Override
		public void onMessage(String arg0) {
			Message msg = this.clientConnection.parse(arg0);
			if (msg instanceof ConfirmMessage)
				ClientManager.Current().Confirms.add((ConfirmMessage) msg);
			// TODO:more message type?
		}

		private void release() {
			this.client.RemoveConnection(this.clientConnection);
			this.clientConnection.clear();
			ClientManager.Current().ClientConnectionPool.Release(this.clientConnection);
		}

	}

}
