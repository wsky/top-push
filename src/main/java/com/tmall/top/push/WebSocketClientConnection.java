package com.tmall.top.push;

import java.nio.ByteBuffer;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jetty.websocket.WebSocket.Connection;
import org.eclipse.jetty.websocket.WebSocket.FrameConnection;

import com.alibaba.fastjson.JSON;
import com.tmall.top.push.messaging.Message;
import com.tmall.top.push.messaging.MessageType;
import com.tmall.top.push.messaging.PublishConfirmMessage;
import com.tmall.top.push.messaging.PublishMessage;

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
		if (StringUtils.isEmpty(this.id))
			return 401;
		return 101;
	}

	public void receive(Receiver receiver, byte[] message, int offset,
			int length) throws Exception {
		int messageType = this.parseMessageType(message[offset]);
		boolean drop = false;
		if (messageType == MessageType.PUBLISH) {
			ByteBuffer buffer = receiver.getPublishBuffer(length);
			if (buffer != null) {
				buffer.put(message, offset, length);
				receiver.receive(this.parse(receiver.acquirePublishMessage(),
						buffer));
			}
		} else if (messageType == MessageType.PUBCONFIRM) {
			ByteBuffer buffer = receiver.getConfirmBuffer(length);
			if (buffer != null) {
				buffer.put(message, offset, length);
				receiver.receive(this.parse(receiver.acquireConfirmMessage(),
						buffer));
			}
		}
		if (drop)
			System.out.println(String.format(
					"no buffer! drop message: messageType=%s", messageType));
	}

	public void receive(Receiver receiver, String message) {
		// use Text-oriented protocol
		if (protocol == "wamp") {
			String[] array = JSON.parseObject(message.toString(),
					String[].class);
			if (Integer.parseInt(array[0]) == MessageType.PUBLISH) {
				receiver.receive(this.parse(receiver.acquirePublishMessage(),
						array));
			} else if (Integer.parseInt(array[0]) == MessageType.PUBCONFIRM) {
				receiver.receive(this.parse(receiver.acquireConfirmMessage(),
						array));
			}
		}
	}

	public String parse(Message message) {
		throw new NotImplementedException();
		// return JSON.toJSONString(message);
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
		msg.messageType =0;
		msg.messageSize=1024;
		msg.to="";
		msg.id="12345";
		msg.body=buffer;
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
