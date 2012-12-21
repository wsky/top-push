package com.tmall.top.push.websocket;

import java.nio.ByteBuffer;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jetty.websocket.WebSocket.Connection;
import org.eclipse.jetty.websocket.WebSocket.FrameConnection;

import com.tmall.top.push.ClientConnection;
import com.tmall.top.push.MessageTooLongException;
import com.tmall.top.push.MessageTypeNotSupportException;
import com.tmall.top.push.NoMessageBufferException;
import com.tmall.top.push.UnauthorizedException;
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

	public void verifyHeaders() throws UnauthorizedException {
		// FIXME:authentication here
		if (StringUtils.isEmpty(this.id))
			throw new UnauthorizedException();
	}

	public Message parse(byte[] message, int offset, int length)
			throws MessageTooLongException, MessageTypeNotSupportException,
			NoMessageBufferException {
		Message msg = this.receiver.parseMessage(this.protocol, message,
				offset, length);
		// must tell who send it
		msg.from = this.getId();
		return msg;
	}

	@Override
	protected void initHeaders() {
		this.id = this.headers.get(ID);
		// TODO:ignore upper/lower case of header
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
