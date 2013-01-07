package com.tmall.top.push.websocket;

import java.nio.ByteBuffer;

import org.eclipse.jetty.websocket.WebSocket.Connection;
import org.eclipse.jetty.websocket.WebSocket.FrameConnection;

import com.tmall.top.push.ClientConnection;
import com.tmall.top.push.MessageTooLongException;
import com.tmall.top.push.NoMessageBufferException;
import com.tmall.top.push.UnauthorizedException;
import com.tmall.top.push.messages.Message;

public class WebSocketClientConnection extends ClientConnection {
	private final static String ORIGIN = "origin";
	// always lower case
	private final static String PROTOCOL = "sec-websocket-protocol";

	private Connection connection;

	public void init(FrameConnection frameConnection) {
		this.receivePing();
	}

	public void init(Connection connection) {
		this.receivePing();
		this.connection = connection;
	}

	public void verifyHeaders() throws UnauthorizedException {
		// FIXME:authentication here
		if (this.id == null || this.id == "")
			throw new UnauthorizedException();
	}

	public Message parse(byte[] message, int offset, int length)
			throws MessageTooLongException, NoMessageBufferException {
		Message msg = this.receiver.parseMessage(this.protocol, message,
				offset, length);
		// must tell who send it
		msg.from = this.getId();
		return msg;
	}

	@Override
	protected void initHeaders() {
		// TODO: how to get id? origin? use connect message instead?
		this.id = this.headers.get(ORIGIN);
		this.protocol = this.headers.get(PROTOCOL);
		this.origin = this.headers.get(ORIGIN);
	}

	@Override
	protected void internalClear() {
		this.connection = null;
	}

	@Override
	public boolean isOpen() {
		return this.connection != null && this.connection.isOpen();
	}

	@Override
	public void sendMessage(Message message) throws Exception {
		int length = message.fullMessageSize;
		ByteBuffer buffer = this.receiver.parseMessage(this.protocol, message);
		this.connection.sendMessage(buffer.array(), buffer.arrayOffset(),
				length);
	}
}
