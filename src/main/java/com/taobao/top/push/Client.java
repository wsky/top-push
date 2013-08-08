package com.taobao.top.push;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Client {
	private Random rd = new Random();
	private int maxPendingCount = 10000;
	private Logger logger;
	private Object id;
	// ping from any connection
	private Date lastPingTime;
	private long totalSendMessageCount;

	private LinkedList<ClientConnection> connections;
	private ConcurrentLinkedQueue<Object> pendingMessages;

	private MessageStateHandler messageStateHandler;
	private ClientStateHandler clientStateHandler;

	public Client(LoggerFactory factory, Object id) {
		this(factory, id, null, null);
	}

	public Client(LoggerFactory factory,
			Object id,
			MessageStateHandler messageStateHandler,
			ClientStateHandler clientStateHandler) {
		this.logger = factory.create(this);
		this.id = id;
		this.receivePing();
		this.connections = new LinkedList<ClientConnection>();
		this.pendingMessages = new ConcurrentLinkedQueue<Object>();
		this.messageStateHandler = messageStateHandler;
		this.clientStateHandler = clientStateHandler;
	}

	public void setMaxPendingCount(int value) {
		this.maxPendingCount = value;
	}

	public Object getId() {
		return this.id;
	}

	public int getPendingMessagesCount() {
		// size() is O(n)
		return this.pendingMessages.size();
	}

	public int getConnectionsCount() {
		return this.connections.size();
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
		if (this.getPendingMessagesCount() >= this.maxPendingCount)
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
		long begin = System.currentTimeMillis();
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
					"flush %s messages to client#%s, cost %sms, totalSendMessageCount=%s",
					temp,
					this.getId(),
					System.currentTimeMillis() - begin,
					this.totalSendMessageCount);
	}

	public void disconnect(String reasonText) {
		this.clearPendingMessages();
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
		this.logger.info("client#%s add new connection from %s",
				this.getId(), conn.getOrigin());
	}

	protected void RemoveConnection(ClientConnection conn) {
		synchronized (this.connections) {
			this.connections.remove(conn);
		}
		this.logger.info("client#%s remove a connection from %s",
				this.getId(), conn.getOrigin());
	}

	protected void SendMessage(CancellationToken token, Object message) {
		// random queue for LRU load-balance
		// LRU queue? https://github.com/wsky/top-push/issues/38
		List<Object> list = Arrays.asList(this.connections.toArray());
		Collections.shuffle(list, this.rd);
		Queue<Object> connectionQueue = new ConcurrentLinkedQueue<Object>(list);
		while (true) {
			if (token.isCancelling()) {
				onDrop(message, "canceled");
				return;
			}

			ClientConnection connection = (ClientConnection) connectionQueue.poll();
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
				// here maybe have many exception, use statusCode instead
				if (!connection.sendMessage(message))
					return;
			} catch (Exception e) {
				onDrop(message, "send message error");
				this.logger.error("send message error", e);
				return; // only send once
			} finally {
				connectionQueue.add(connection);
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
