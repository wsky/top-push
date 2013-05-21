package com.taobao.top.push.websocket;

import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import com.taobao.top.push.Client;
import com.taobao.top.push.ClientConnection;
import com.taobao.top.push.ClientStateHandler;
import com.taobao.top.push.DefaultIdentity;
import com.taobao.top.push.DefaultLoggerFactory;
import com.taobao.top.push.Identity;
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
				this.get(config, "maxConnectionCount"),
				this.get(config, "senderCount"),
				this.get(config, "senderIdle"),
				this.get(config, "stateBuilderIdle"));
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
			public Identity onClientConnecting(Map<String, String> headers) throws Exception {
				String id = headers.get("origin");

				if (id == null || id.trim() == "")
					throw new Exception("origin is empty");

				return new DefaultIdentity(id);
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
			public void onSent(Identity client, Object message) {
				if (message instanceof Message)
					receiver.release((Message) message);
			}

			@Override
			public void onDropped(Identity client, Object message, String reason) {
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
