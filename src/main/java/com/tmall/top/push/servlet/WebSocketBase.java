package com.tmall.top.push.servlet;

import org.eclipse.jetty.websocket.WebSocket;
import com.tmall.top.push.Client;
import com.tmall.top.push.PushManager;
import com.tmall.top.push.Receiver;
import com.tmall.top.push.WebSocketClientConnection;

public class WebSocketBase implements WebSocket.OnTextMessage,
		WebSocket.OnControl, WebSocket.OnFrame {

	private Receiver receiver;
	private FrameConnection frameConnection;
	private Client client;
	private WebSocketClientConnection clientConnection;

	public WebSocketBase(Receiver receiver, Client client,
			WebSocketClientConnection clientConnection) {
		this.receiver = receiver;
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
		this.clientConnection.receive(receiver, arg0);
	}

	@Override
	public boolean onFrame(byte arg0, byte arg1, byte[] arg2, int arg3, int arg4) {
		return false;
	}

	private void release() {
		this.client.RemoveConnection(this.clientConnection);
		this.clientConnection.clear();
		PushManager.Current().ClientConnectionPool
				.release(this.clientConnection);
	}
}
