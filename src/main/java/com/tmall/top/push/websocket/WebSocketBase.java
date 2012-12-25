package com.tmall.top.push.websocket;

import org.eclipse.jetty.websocket.WebSocket;

import com.tmall.top.push.Client;
import com.tmall.top.push.PushManager;
import com.tmall.top.push.MessageTooLongException;
import com.tmall.top.push.MessageTypeNotSupportException;
import com.tmall.top.push.NoMessageBufferException;
import com.tmall.top.push.UnauthorizedException;
import com.tmall.top.push.messages.Message;

public abstract class WebSocketBase implements WebSocket.OnTextMessage,
		WebSocket.OnBinaryMessage, WebSocket.OnControl, WebSocket.OnFrame {

	protected FrameConnection frameConnection;
	protected Connection connection;

	protected PushManager manager;
	protected Client client;
	protected WebSocketClientConnection clientConnection;

	public WebSocketBase(PushManager manager, Client client,
			WebSocketClientConnection clientConnection) {
		this.manager = manager;
		this.client = client;
		this.clientConnection = clientConnection;
	}

	@Override
	public void onClose(int closeCode, String message) {
		this.release();
	}

	@Override
	public void onHandshake(FrameConnection frameConnection) {
		try {
			this.clientConnection.verifyHeaders();
		} catch (UnauthorizedException e) {
			this.release();
			frameConnection.close(401, "invalid header");
			return;
		}

		if (this.manager.isReachMaxConnectionCount()) {
			this.release();
			frameConnection.close(403, "reach max connections");
			return;
		}

		this.frameConnection = frameConnection;
		this.clientConnection.init(this.frameConnection);
		this.client.AddConnection(this.clientConnection);
	}

	@Override
	public void onOpen(Connection connection) {
		this.connection = connection;
		this.clientConnection.init(connection);
	}

	@Override
	public boolean onControl(byte arg0, byte[] arg1, int arg2, int arg3) {
		if (this.frameConnection.isPing(arg0)) {
			this.receivePing();
		}
		return false;
	}

	@Override
	public abstract void onMessage(String message);

	@Override
	public void onMessage(byte[] data, int offset, int length) {
		// any message use as ping
		this.receivePing();

		try {
			Message msg = this.clientConnection.parse(data, offset, length);
			// deliver to target client
			this.manager.getClient(msg.to).pendingMessage(msg);
			System.out.println(String.format(
					"client#%s got an pending message(%s) from %s %s %s", msg.to,
					msg, msg.from, msg.remainingLength,msg.fullMessageSize));
		} catch (MessageTooLongException e) {
			e.printStackTrace();
			this.frameConnection.close(400, e.getMessage());
		} catch (MessageTypeNotSupportException e) {
			e.printStackTrace();
			this.frameConnection.close(400, e.getMessage());
		} catch (NoMessageBufferException e) {
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
