package com.tmall.top.websocket;

import java.nio.ByteBuffer;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jetty.websocket.WebSocket.Connection;
import org.eclipse.jetty.websocket.WebSocket.FrameConnection;

import com.alibaba.fastjson.JSON;
import com.tmall.top.push.ClientConnection;
import com.tmall.top.push.Receiver;
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
			throws Exception {
		int messageType = this.parseMessageType(message[offset]);

		boolean drop = false;
		if (messageType == MessageType.PUBLISH) {
			ByteBuffer buffer = this.receiver.getPublishBuffer(length);
			if (buffer != null) {
				buffer.put(message, offset, length);
				return this
						.parse(this.receiver.acquirePublishMessage(), buffer);
			} else {
				drop = true;
			}

		} else if (messageType == MessageType.PUBCONFIRM) {
			ByteBuffer buffer = this.receiver.getConfirmBuffer(length);
			if (buffer != null) {
				buffer.put(message, offset, length);
				return this
						.parse(this.receiver.acquireConfirmMessage(), buffer);
			} else {
				drop = true;
			}
		}
		if (drop)
			System.out.println(String.format(
					"no buffer! drop message: messageType=%s", messageType));

		return null;
	}

	public Message parse(String message) {
		throw new NotImplementedException();
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
		throw new NotImplementedException();
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

	private PublishMessage parse(PublishMessage msg, String[] array) {
		// TODO:fill msg by protocol
		return msg;
	}

	private PublishConfirmMessage parse(PublishConfirmMessage msg,
			String[] array) {
		return msg;
	}

	private PublishMessage parse(PublishMessage msg, ByteBuffer buffer) {
		// TODO: fill msg from buffer by protocol
		msg.messageType = 0;
		msg.messageSize = 1024;
		msg.to = "";
		msg.id = "12345";
		msg.body = buffer;
		return msg;
	}

	private PublishConfirmMessage parse(PublishConfirmMessage msg,
			ByteBuffer buffer) {
		return msg;
	}

	private int parseMessageType(byte headerByte) {
		return (headerByte & 240) >> 4;
	}
}
