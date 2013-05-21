package com.taobao.top.push.websocket;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketServlet;

import com.taobao.top.push.Client;
import com.taobao.top.push.Logger;
import com.taobao.top.push.LoggerFactory;
import com.taobao.top.push.PushManager;

public class FrontendServlet extends WebSocketServlet {

	private static final long serialVersionUID = -2545213842937092374L;

	private Logger logger;
	private WebSocket webSocket;

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PushManager manager = InitServlet.manager;
		this.logger = manager.getLoggerFactory().create(this);

		HashMap<String, String> headers = Utils.parseHeaders(request);
		this.dump(headers);

		WebSocketClientConnection clientConnection = Utils.getClientConnectionPool().acquire();
		try {
			this.webSocket = new FrontendWebSocket(
					manager.getLoggerFactory(),
					manager,
					manager.connectClient(headers, clientConnection),
					clientConnection, 
					InitServlet.receiver, 
					InitServlet.processor);
		} catch (Exception e) {
			Utils.getClientConnectionPool().release(clientConnection);
			response.sendError(401, e.getMessage());
			this.logger.error(e);
			return;
		}

		super.service(request, response);
	}

	@Override
	public boolean checkOrigin(HttpServletRequest request, String origin) {
		return true;
	}

	@Override
	public WebSocket doWebSocketConnect(HttpServletRequest request, String protocol) {
		return this.webSocket;
	}

	private void dump(HashMap<String, String> headers) {
		if (!this.logger.isDebugEnable())
			return;
		for (Entry<String, String> h : headers.entrySet()) {
			this.logger.debug("%s=%s", h.getKey(), h.getValue());
		}
	}

	private class FrontendWebSocket extends WebSocketBase {
		public FrontendWebSocket(LoggerFactory loggerFactory,
				PushManager manager,
				Client client,
				WebSocketClientConnection clientConnection,
				Receiver receiver,
				Processor processor) {
			super(loggerFactory, manager, client, clientConnection, receiver, processor);
		}
	}

}
