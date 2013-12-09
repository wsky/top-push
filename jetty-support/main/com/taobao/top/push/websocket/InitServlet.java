package com.taobao.top.push.websocket;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import com.taobao.top.push.Client;
import com.taobao.top.push.ClientConnection;
import com.taobao.top.push.ClientStateHandler;
import com.taobao.top.push.DefaultLoggerFactory;
import com.taobao.top.push.LoggerFactory;
import com.taobao.top.push.MessageStateHandler;
import com.taobao.top.push.PushManager;
import com.taobao.top.push.messages.Message;

public class InitServlet extends HttpServlet {

	private static final long serialVersionUID = 3059398081890461730L;

	public static LoggerFactory loggerFactory;
	public static PushManager manager;
	public static Receiver receiver;
	public static Processor processor;

	@Override
	public void init(ServletConfig config) throws ServletException {
		loggerFactory = new DefaultLoggerFactory(false, true, true, true, true);
		Utils.initClientConnectionPool(100000);
		receiver = new Receiver(this.get(config, "maxMessageSize"), this.get(config, "maxMessageBufferCount"));
		processor = new Processor();
		manager = new PushManager(
				loggerFactory,
				this.get(config, "senderCount"),
				this.get(config, "stateBuilderIdle"));
		manager.setMaxConnectionCount(this.get(config, "maxConnectionCount"));
		manager.setClientStateHandler(new ClientStateHandler() {
			@Override
			public void onClientPending(Client client) {
			}

			@Override
			public void onClientOffline(Client client) {
			}

			@Override
			public void onClientIdle(Client client) {
			}
			
			@Override
			public void onClientConnect(Client client, ClientConnection clientConnection) {
			}

			@Override
			public void onClientDisconnect(Client client, ClientConnection clientConnection) {
				if (clientConnection instanceof WebSocketClientConnection) {
					clientConnection.clear();
					Utils.getClientConnectionPool().release((WebSocketClientConnection) clientConnection);
				}
			}
		});
		manager.setMessageStateHandler(new MessageStateHandler() {
			@Override
			public void onSent(Object client, Object message) {
				if (message instanceof Message)
					receiver.release((Message) message);
			}

			@Override
			public void onDropped(Object client, Object message, String reason) {
				if (message instanceof Message)
					receiver.release((Message) message);
			}
		});
		super.init(config);
	}

	private int get(ServletConfig config, String k) {
		return Integer.parseInt(config.getInitParameter(k));
	}
}
