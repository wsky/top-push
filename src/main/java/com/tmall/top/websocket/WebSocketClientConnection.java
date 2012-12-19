package com.tmall.top.websocket;

import java.nio.ByteBuffer;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jetty.websocket.WebSocket.Connection;
import org.eclipse.jetty.websocket.WebSocket.FrameConnection;

import com.tmall.top.push.ClientConnection;
import com.tmall.top.push.messageTooLongException;
import com.tmall.top.push.messages.Message;
import com.tmall.top.push.messages.MessageType;
import com.tmall.top.push.messages.PublishConfirmMessage;
import com.tmall.top.push.messages.PublishMessage;

public class WebSocketClientConnection extends ClientConnection {
	private final static String ID = "id";
	private final static String ORIGIN = "Origin";
	private final static String PROTOCOL = "Sec-WebSocket-Protocol";

	private FrameConnection frameConnection;
	private Connection connection;

	public void init(FrameConnection frameConnection) {
		this.ReceivePing();
		this.frameConnection = frameConnection;
	}

	public void init(Connection connection) {
		this.ReceivePing();
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
		int messageType = this.parseMessageType(message[offset]);

		Message msg = null;
		ByteBuffer buffer = null;

		if (messageType == MessageType.PUBLISH) {
			buffer = this.receiver.getPublishBuffer(length);
			msg = this.receiver.acquirePublishMessage();
		} else if (messageType == MessageType.PUBCONFIRM) {
			buffer = this.receiver.getConfirmBuffer(length);
			msg = this.receiver.acquireConfirmMessage();
		}

		if (msg == null) {
			System.out.println(String.format(
					"not support message: messageType=%s", messageType));
		} else if (buffer != null) {
			buffer.put(message, offset, length);
			msg = this.parse(msg, buffer);
		} else {
			System.out.println(String.format(
					"no buffer! drop message: messageType=%s", messageType));
		}
		return msg;
	}

	public Message parse(String message) {
		return null;
		/*
		 * Receiver receiver = this.manager.getReceiver(); // use Text-oriented
		 * protocol if (protocol == "wamp") { String[] array =
		 * JSON.parseObject(message.toString(), String[].class); if
		 * (Integer.parseInt(array[0]) == MessageType.PUBLISH) { return
		 * this.parse(receiver.acquirePublishMessage(), array); } else if
		 * (Integer.parseInt(array[0]) == MessageType.PUBCONFIRM) { return
		 * this.parse(receiver.acquireConfirmMessage(), array); } } return null;
		 */
	}

	public String parse(Message message) {
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
		this.frameConnection = null;
		this.connection = null;
	}

	@Override
	public boolean isOpen() {
		return this.connection.isOpen();
	}

	@Override
	public void sendMessage(Message msg) throws Exception {
		this.connection.sendMessage(this.parse(msg));
	}

	// TODO: fill message from buffer by custom protocol
	private Message parse(Message msg, ByteBuffer buffer) {
		msg.messageType = 0;
		msg.messageSize = 1024;
		msg.to = "";
		msg.body = buffer;

		if (msg instanceof PublishMessage) {
			PublishMessage publishMessage = (PublishMessage) msg;
			publishMessage.id = "12345";
		} else if (msg instanceof PublishConfirmMessage) {
			PublishConfirmMessage confirmMessage = (PublishConfirmMessage) msg;
			confirmMessage.confirmId = "1,2,3";
		}

		return msg;
	}

	private int parseMessageType(byte headerByte) {
		return (headerByte & 240) >> 4;
	}
}
