package com.tmall.top.push;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jetty.websocket.WebSocket.Connection;
import org.eclipse.jetty.websocket.WebSocket.FrameConnection;

import com.alibaba.fastjson.JSON;
import com.tmall.top.push.messaging.Message;

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
		if (StringUtils.isEmpty(this.id))
			return 401;
		return 101;
	}

	// TODO:parse by protocol
	public Message parse(String message) {
		// if(protocol=="mqtt")
		// if(protocol=="wamp")
		String[] arrStrings = JSON.parseObject(message, String[].class);
		return null;
	}
	public String parse(Message message) {
		// if(protocol=="mqtt")
		// if(protocol=="wamp")
		return JSON.toJSONString(message);
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
}
