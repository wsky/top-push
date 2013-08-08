package com.taobao.top.push.websocket;

import java.nio.ByteBuffer;

import org.eclipse.jetty.websocket.WebSocket.Connection;
import org.eclipse.jetty.websocket.WebSocket.FrameConnection;

import com.taobao.top.push.ClientConnection;
import com.taobao.top.push.messages.Message;

public class WebSocketClientConnection extends ClientConnection {
	private final static String ORIGIN = "origin";
	// always lower case
	private final static String PROTOCOL = "sec-websocket-protocol";

	private Connection wsConnection;
	private FrameConnection frameConnection;
	private Receiver receiver;

	public void init(FrameConnection frameConnection) {
		this.frameConnection = frameConnection;
		this.receivePing();
	}

	public void init(Connection wsConnection, Receiver receiver) {
		this.receivePing();
		this.wsConnection = wsConnection;
		this.receiver = receiver;
	}

	public Message parse(byte[] data, int offset, int length)
			throws MessageTooLongException, NoMessageBufferException {
		Message msg = this.receiver.parseMessage(this.protocol, data, offset,
				length);
		// must tell who send it
		msg.from = this.getId().toString();
		return msg;
	}

	@Override
	protected void initHeaders() {
		this.protocol = this.headers.get(PROTOCOL);
		this.origin = this.headers.get(ORIGIN);
	}

	@Override
	protected void internalClear() {
		this.wsConnection = null;
	}

	@Override
	public boolean isOpen() {
		return this.wsConnection != null && this.wsConnection.isOpen();
	}

	@Override
	public boolean sendMessage(Object message) throws Exception {
		Message msg = (Message) message;
		int length = msg.fullMessageSize;
		ByteBuffer buffer = this.receiver.parseMessage(this.protocol, msg);
		this.wsConnection.sendMessage(buffer.array(), buffer.arrayOffset(), length);
		return true;
	}

	@Override
	public void close(String reasonText) {
		this.frameConnection.close(1004, reasonText);
	}
}
