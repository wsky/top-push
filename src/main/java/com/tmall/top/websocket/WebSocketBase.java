package com.tmall.top.websocket;

import org.eclipse.jetty.websocket.WebSocket;

import com.tmall.top.push.Client;

public class WebSocketBase implements WebSocket.OnTextMessage,
		WebSocket.OnBinaryMessage, WebSocket.OnControl, WebSocket.OnFrame {

	private FrameConnection frameConnection;

	private Client client;
	private WebSocketClientConnection clientConnection;

	public WebSocketBase(Client client,
			WebSocketClientConnection clientConnection) {
		this.client = client;
		this.clientConnection = clientConnection;
	}

	@Override
	public void onClose(int arg0, String arg1) {
		this.release();
	}

	@Override
	public void onHandshake(FrameConnection arg0) {
		int statusCode = this.clientConnection.verifyHeaders();
		if (statusCode != 101) {
			arg0.close(statusCode, "invalid header");
			this.release();
			return;
		}
		this.frameConnection = arg0;
		this.clientConnection.init(this.frameConnection);
		this.client.AddConnection(this.clientConnection);
	}

	@Override
	public void onOpen(Connection arg0) {
		this.clientConnection.init(arg0);

	}

	@Override
	public boolean onControl(byte arg0, byte[] arg1, int arg2, int arg3) {
		if (this.frameConnection.isPing(arg0)) {
			this.clientConnection.ReceivePing();
		}
		return false;
	}

	@Override
	public void onMessage(String arg0) {
		// Text-oriented protocol can use this way
		this.client.pendingMessage(this.clientConnection.parse(arg0));
	}

	@Override
	public void onMessage(byte[] data, int offset, int length) {
		// TODO: jetty not support subprotocol friendly, then, will course
		// unnecessary copy
		try {
			this.client.pendingMessage(this.clientConnection.parse(data,
					offset, length));
		} catch (Exception e) {
			e.printStackTrace();
			this.frameConnection.close(400, "message too long");
		}
	}

	@Override
	public boolean onFrame(byte arg0, byte arg1, byte[] arg2, int arg3, int arg4) {
		return false;
	}

	private void release() {
		this.client.RemoveConnection(this.clientConnection);
		this.clientConnection.clear();
		Utils.getClientConnectionPool().release(this.clientConnection);
	}
}
