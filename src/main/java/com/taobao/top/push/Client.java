package com.taobao.top.push;

import java.util.Date;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Client {
	private final static int MAX_PENDING_COUNT = 10000;
	private Logger logger;
	private Identity id;
	// ping from any connection
	private Date lastPingTime;
	private int totalSendMessageCount;

	private LinkedList<ClientConnection> connections;
	private ConcurrentLinkedQueue<ClientConnection> connectionQueue;
	private ConcurrentLinkedQueue<Object> pendingMessages;

	private MessageStateHandler messageStateHandler;
	private ClientStateHandler clientStateHandler;

	public Client(LoggerFactory factory, Identity id) {
		this(factory, id, null, null);
	}

	public Client(LoggerFactory factory,
			Identity id,
			MessageStateHandler messageStateHandler,
			ClientStateHandler clientStateHandler) {
		this.logger = factory.create(this);
		this.id = id;
		this.receivePing();
		this.connections = new LinkedList<ClientConnection>();
		this.connectionQueue = new ConcurrentLinkedQueue<ClientConnection>();
		this.pendingMessages = new ConcurrentLinkedQueue<Object>();
		this.messageStateHandler = messageStateHandler;
		this.clientStateHandler = clientStateHandler;
	}

	public Identity getId() {
		return this.id;
	}

	public int getPendingMessagesCount() {
		// size() is O(n)
		return this.pendingMessages.size();
	}

	public int getConnectionsCount() {
		return this.connections.size();
	}

	protected int getConnectionQueueSize() {
		return this.connectionQueue.size();
	}

	public Date getLastPingTime() {
		return this.lastPingTime;
	}

	public void receivePing() {
		this.lastPingTime = new Date();
	}

	// pend message waiting to be send
	public boolean pendingMessage(Object message) {
		if (message == null)
			return false;
		if (this.getPendingMessagesCount() >= MAX_PENDING_COUNT)
			return false;
		this.pendingMessages.add(message);
		return true;
	}

	public void clearPendingMessages() {
		Object msg;
		while ((msg = this.pendingMessages.poll()) != null)
			if (this.messageStateHandler != null)
				this.messageStateHandler.onDropped(this.id, msg, "clearPendingMessages");
	}

	public void flush(CancellationToken token, int count) {
		int temp = 0;
		for (int i = 0; i < count; i++) {
			if (token.isCancelling())
				break;
			Object msg = this.pendingMessages.poll();
			if (msg == null)
				break;
			this.SendMessage(token, msg);
			temp++;
		}
		this.totalSendMessageCount += temp;
		if (temp > 0)
			this.logger.info(
					"flush %s messages to client#%s, totalSendMessageCount=%s",
					temp,
					this.getId(),
					this.totalSendMessageCount);
	}

	public void disconnect(String reasonText) {
		this.clearPendingMessages();
		this.connectionQueue.clear();
		int size = this.connections.size();
		for (int i = 0; i < size; i++) {
			try {
				ClientConnection connection = this.connections.get(i);
				connection.close(reasonText);
				this.onDisconnect(connection);
			} catch (IndexOutOfBoundsException e) {
				break;
			} catch (Exception e) {
				this.logger.error(e);
			}
		}
		this.connections.clear();
	}

	protected void AddConnection(ClientConnection conn) {
		synchronized (this.connections) {
			this.connections.add(conn);
		}
		this.connectionQueue.add(conn);
		this.logger.info("client#%s add new connection from %s",
				this.getId(), conn.getOrigin());
	}

	protected void RemoveConnection(ClientConnection conn) {
		synchronized (this.connections) {
			this.connections.remove(conn);
		}
		this.connectionQueue.remove(conn);
		this.logger.info("client#%s remove a connection from %s",
				this.getId(), conn.getOrigin());
	}

	protected void SendMessage(CancellationToken token, Object message) {
		// FIFO queue for LRU load-balance
		while (true) {
			if (token.isCancelling()) {
				onDrop(message, "canceled");
				return;
			}

			ClientConnection connection = this.connectionQueue.poll();
			if (connection == null) {
				onDrop(message, "no valid connection");
				return;
			}

			if (!connection.isOpen()) {
				this.RemoveConnection(connection);
				this.onDisconnect(connection);
				this.logger.info("connection#%s[%s] is closed, remove it",
						connection.getId(),
						connection.getOrigin());
				continue;
			}

			try {
				connection.sendMessage(message);
			} catch (Exception e) {
				onDrop(message, "send message error");
				this.logger.error("send message error", e);
				return; // only send once
			} finally {
				this.connectionQueue.add(connection);
			}

			this.onSent(message);
			return;
		}
	}

	private void onDrop(Object message, String reason) {
		if (this.messageStateHandler != null)
			this.messageStateHandler.onDropped(this.id, message, reason);
	}

	private void onSent(Object message) {
		if (this.messageStateHandler != null)
			this.messageStateHandler.onSent(this.id, message);
	}

	private void onDisconnect(ClientConnection connection) {
		if (this.clientStateHandler != null)
			this.clientStateHandler.onClientDisconnect(this, connection);
	}
}
