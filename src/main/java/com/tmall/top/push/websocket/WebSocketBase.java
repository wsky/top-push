package com.tmall.top.push.websocket;

import org.eclipse.jetty.websocket.WebSocket;

import com.tmall.top.push.Client;
import com.tmall.top.push.PushManager;
import com.tmall.top.push.messageTooLongException;
import com.tmall.top.push.messages.Message;

public class WebSocketBase implements WebSocket.OnTextMessage,
		WebSocket.OnBinaryMessage, WebSocket.OnControl, WebSocket.OnFrame {

	private FrameConnection frameConnection;

	private PushManager manager;
	private Client client;
	private WebSocketClientConnection clientConnection;

	public WebSocketBase(PushManager manager, Client client,
			WebSocketClientConnection clientConnection) {
		this.manager = manager;
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
			this.receivePing();
		}
		return false;
	}

	@Override
	public void onMessage(String arg0) {
		this.receivePing();
	}

	@Override
	public void onMessage(byte[] data, int offset, int length) {
		this.receivePing();
		// TODO: jetty not support subprotocol friendly, then, will course
		// unnecessary copy?
		try {
			Message msg = this.clientConnection.parse(data, offset, length);
			this.manager.getClient(msg.to).pendingMessage(msg);
		} catch (messageTooLongException e) {
			e.printStackTrace();
			this.frameConnection.close(400, e.getMessage());
		}
	}

	@Override
	public boolean onFrame(byte arg0, byte arg1, byte[] arg2, int arg3, int arg4) {
		return false;
	}

	// telling client is alive
	private void receivePing() {
		this.clientConnection.receivePing();
		this.client.receivePing();
	}

	private void release() {
		this.client.RemoveConnection(this.clientConnection);
		this.clientConnection.clear();
		Utils.getClientConnectionPool().release(this.clientConnection);
	}
}
