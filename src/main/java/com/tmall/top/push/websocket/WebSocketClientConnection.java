package com.tmall.top.push.websocket;

import java.nio.ByteBuffer;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jetty.websocket.WebSocket.Connection;
import org.eclipse.jetty.websocket.WebSocket.FrameConnection;

import com.tmall.top.push.ClientConnection;
import com.tmall.top.push.messageTooLongException;
import com.tmall.top.push.messages.Message;

public class WebSocketClientConnection extends ClientConnection {
	private final static String ID = "id";
	private final static String ORIGIN = "Origin";
	private final static String PROTOCOL = "Sec-WebSocket-Protocol";

	private Connection connection;

	public void init(FrameConnection frameConnection) {
		this.receivePing();
	}

	public void init(Connection connection) {
		this.receivePing();
		this.connection = connection;
	}

	public int verifyHeaders() {
		// TODO:authentication here
		// TODO:define error code
		if (StringUtils.isEmpty(this.id))
			return 401;
		return 101;
	}

	public Message parse(byte[] message, int offset, int length)
			throws messageTooLongException {
		return this.receiver.parseMessage(this.protocol, message, offset,
				length);
	}

	public Message parse(String message) {
		return null;
	}

	@Override
	protected void initHeaders() {
		this.id = this.headers.get(ID);
		this.protocol = this.headers.get(PROTOCOL);
		this.origin = this.headers.get(ORIGIN);
	}

	@Override
	protected void internalClear() {
		this.connection = null;
	}

	@Override
	public boolean isOpen() {
		return this.connection.isOpen();
	}

	@Override
	public void sendMessage(Message message) throws Exception {
		ByteBuffer buffer = this.receiver.parseMessage(this.protocol, message);
		this.connection.sendMessage(buffer.array(), buffer.arrayOffset(),
				message.messageSize);
	}
}
