package com.taobao.top.push.websocket;

import org.eclipse.jetty.websocket.WebSocket;

import com.taobao.top.push.Client;
import com.taobao.top.push.DefaultIdentity;
import com.taobao.top.push.Logger;
import com.taobao.top.push.LoggerFactory;
import com.taobao.top.push.PushManager;
import com.taobao.top.push.messages.Message;

public abstract class WebSocketBase implements WebSocket.OnTextMessage,
		WebSocket.OnBinaryMessage,
		WebSocket.OnControl,
		WebSocket.OnFrame {
	protected Logger logger;

	protected FrameConnection frameConnection;
	protected Connection connection;

	protected PushManager manager;
	protected Processor processor;
	protected Receiver receiver;

	protected Object clientId;
	protected Client client;
	protected WebSocketClientConnection clientConnection;

	public WebSocketBase(LoggerFactory loggerFactory,
			PushManager manager,
			Object clientId,
			WebSocketClientConnection clientConnection,
			Receiver receiver,
			Processor processor) {
		this.logger = loggerFactory.create(this);
		this.manager = manager;
		this.clientId = clientId;
		this.clientConnection = clientConnection;
		this.receiver = receiver;
		this.processor = processor;
	}

	@Override
	public void onClose(int closeCode, String message) {
		this.manager.disconnectClient(this.client, this.clientConnection);
		this.clear();
		this.logger.info("websocket closed: %s | %s", closeCode, message);
	}

	@Override
	public void onHandshake(FrameConnection frameConnection) {
		if (this.manager.isReachMaxConnectionCount()) {
			this.clear();
			frameConnection.close(403, "reach max connections");
			this.logger.warn("close websocket: %s | %s", 403, "reach max connections");
			return;
		}
		this.frameConnection = frameConnection;
		this.clientConnection.init(this.frameConnection);
	}

	@Override
	public void onOpen(Connection connection) {
		this.connection = connection;
		this.clientConnection.init(this.connection, this.receiver);
		this.client = this.manager.connectClient(this.clientId, clientConnection);
	}

	@Override
	public boolean onControl(byte arg0, byte[] arg1, int arg2, int arg3) {
		if (this.frameConnection.isPing(arg0)) {
			this.receivePing();
			return true;
		}
		return false;
	}

	@Override
	public void onMessage(String message) {
		this.logger.warn("receive text message: %s", message);
	}

	@Override
	public void onMessage(byte[] data, int offset, int length) {
		// any message use as ping
		this.receivePing();

		Message msg = null;
		try {
			msg = this.clientConnection.parse(data, offset, length);
		} catch (MessageTooLongException e) {
			this.logger.error(e);
			this.frameConnection.close(400, e.getMessage());
		} catch (NoMessageBufferException e) {
			this.logger.error(e);
			// https://github.com/wsky/top-push/issues/23
			// ignore no buffer and drop it
			// this.frameConnection.close(400, e.getMessage());
		}

		try {
			if (this.processor != null &&
					!this.processor.process(msg, this.clientConnection)) {
				// forward message
				// deliver to target client
				Client client = this.manager.getClient(new DefaultIdentity(msg.to));
				if (client != null)
					client.pendingMessage(msg);
				else
					this.receiver.release(msg);
			}
		} catch (Exception e) {
			this.receiver.release(msg);
			this.logger.error(e);
		}
	}

	@Override
	public boolean onFrame(byte flags, byte opcode, byte[] array, int offset, int length) {
		int FIN = flags & 0x8;
		if (FIN == 0)
			this.logger.error("FIN=%s|OpCode=%s|length=%s", FIN, opcode, length);
		return false;
	}

	// telling client is alive
	private void receivePing() {
		this.clientConnection.receivePing();
		this.client.receivePing();
	}

	private void clear() {
		Utils.getClientConnectionPool().release(this.clientConnection);
	}
}
