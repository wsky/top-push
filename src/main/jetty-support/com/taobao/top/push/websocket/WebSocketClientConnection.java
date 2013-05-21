package com.taobao.top.push.websocket;

import java.nio.ByteBuffer;

import org.eclipse.jetty.websocket.WebSocket.Connection;
import org.eclipse.jetty.websocket.WebSocket.FrameConnection;

import com.taobao.top.push.ClientConnection;
import com.taobao.top.push.MessageTooLongException;
import com.taobao.top.push.NoMessageBufferException;
import com.taobao.top.push.messages.Message;

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

	public Message parse(byte[] data, int offset, int length)
			throws MessageTooLongException, NoMessageBufferException {
		Message msg = this.receiver.parseMessage(this.protocol, data, offset,
				length);
		// must tell who send it
		msg.from = this.getId();
		return msg;
	}

	@Override
	protected void initHeaders() {
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
