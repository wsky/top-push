package com.tmall.top.push.websocket;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketServlet;

import com.tmall.top.push.Client;
import com.tmall.top.push.PushManager;

public class BackendServlet extends WebSocketServlet {

	private static final long serialVersionUID = 3431855312865710986L;

	@Override
	public WebSocket doWebSocketConnect(HttpServletRequest arg0, String arg1) {
		PushManager manager = PushManager.current();
		WebSocketClientConnection clientConnection = Utils
				.getClientConnectionPool().acquire();
		clientConnection.init(Utils.parseHeaders(arg0), manager);

		return new BackendWebSocket(manager, manager.getClient(clientConnection
				.getId()), clientConnection);
	}

	public class BackendWebSocket extends WebSocketBase {

		public BackendWebSocket(PushManager manager, Client client,
				WebSocketClientConnection clientConnection) {
			super(manager, client, clientConnection);
		}

		// TODO: implement an easy RPC for publisher
		/*
		 * publish(to, msgs){ //conns is FIFO queue with easy loadbalance
		 * for(var i=0;i<conns.length;i++){ if(conns[i].isConnected(to)) {
		 * conns[i].sendMesage(msg); } } } or client.getConn(id).send(msg);
		 */

	}
}
