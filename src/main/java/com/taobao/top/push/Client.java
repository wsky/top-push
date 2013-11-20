package com.taobao.top.push;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.taobao.top.push.ClientConnection.SendStatus;

public class Client {
	private Random rd = new Random();
	private int maxPendingCount = 10000;
	private Logger logger;
	private Object id;
	// ping from any connection
	private Date lastPingTime;
	private long totalSendMessageCount;

	private LinkedList<ClientConnection> connections;
	private Queue<Object> pendingMessages;

	private MessageStateHandler messageStateHandler;
	private ClientStateHandler clientStateHandler;

	private Map<Object, Object> state;

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
		this.state = new ConcurrentHashMap<Object, Object>();
	}

	public void setMaxPendingCount(int value) {
		this.maxPendingCount = value;
	}

	public Object getId() {
		return this.id;
	}

	public Map<Object, Object> getState() {
		return this.state;
	}

	public long getTotalSendMessageCount() {
		return this.totalSendMessageCount;
	}

	public int getPendingMessagesCount() {
		// size() is O(n)
		return this.pendingMessages.size();
	}

	public int getConnectionsCount() {
		return this.connections.size();
	}

	public ClientConnection[] getConnections() {
		return this.connections.toArray(new ClientConnection[0]);
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
		return this.pendingMessages.add(message);
	}

	public void clearPendingMessages() {
		Object msg;
		while ((msg = this.pendingMessages.poll()) != null)
			if (this.messageStateHandler != null)
				this.messageStateHandler.onDropped(this.id, msg, "clearPendingMessages");
	}

	public void flush(CancellationToken token, int count) {
		// random queue for LRU load-balance
		// LRU queue? https://github.com/wsky/top-push/issues/38
		List<ClientConnection> list = Arrays.asList(this.connections.toArray(new ClientConnection[0]));
		Collections.shuffle(list, this.rd);
		Queue<ClientConnection> connectionQueue = new ConcurrentLinkedQueue<ClientConnection>(list);

		int temp = 0;
		long begin = System.currentTimeMillis();
		for (int i = 0; i < count; i++) {
			if (token.isCancelling())
				break;
			if (connectionQueue.size() == 0)
				break;

			Object msg = this.pendingMessages.poll();
			if (msg == null)
				break;
			// pass pre-prepared connections that avoid concurrent
			// and work well for flush continuing flag
			if (!this.SendMessage(token, msg, connectionQueue))
				break;
			temp++;
		}
		this.totalSendMessageCount += temp;

		if (temp > 0 && this.logger.isInfoEnabled())
			this.logger.info(
					"flush %s messages to client#%s, cost %sms, totalSendMessageCount=%s",
					temp, this.getId(),
					System.currentTimeMillis() - begin,
					this.totalSendMessageCount);
	}

	public synchronized void disconnect(String reasonText) {
		this.clearPendingMessages();
		int size = this.connections.size();
		for (int i = 0; i < size; i++) {
			try {
				ClientConnection connection = this.connections.get(i);
				connection.close(reasonText);
				this.onDisconnect(connection);
				this.logger.info("client#%s disconnect a connection from %s: %s",
						this.getId(),
						connection.getOrigin(),
						reasonText);
			} catch (IndexOutOfBoundsException e) {
				break;
			} catch (Exception e) {
				this.logger.error(e);
			}
		}
		this.connections.clear();
	}

	protected synchronized void AddConnection(ClientConnection conn) {
		this.connections.add(conn);
		this.logger.info("client#%s add new connection from %s",
				this.getId(), conn.getOrigin());
		this.onConnect(conn);
	}

	protected synchronized void RemoveConnection(ClientConnection conn) {
		this.connections.remove(conn);
		this.logger.info("client#%s remove a connection from %s",
				this.getId(), conn.getOrigin());
		this.onDisconnect(conn);
	}

	protected synchronized int cleanConnections() {
		List<ClientConnection> trash = new ArrayList<ClientConnection>();
		Iterator<ClientConnection> iterator = this.connections.iterator();
		while (iterator.hasNext()) {
			ClientConnection clientConnection = iterator.next();
			if (!clientConnection.isOpen())
				trash.add(clientConnection);
		}

		for (ClientConnection c : trash)
			this.RemoveConnection(c);
		return this.connections.size();
	}

	// result is that weather to continue flushing
	protected boolean SendMessage(CancellationToken token, Object message, Queue<ClientConnection> connectionQueue) {
		while (true) {
			if (token.isCancelling()) {
				onDrop(message, "canceled");
				return false;
			}

			ClientConnection connection = (ClientConnection) connectionQueue.poll();
			if (connection == null) {
				this.logger.info(String.format(
						"client#%s no valid connection, drop message: %s",
						this.getId(), message));
				onDrop(message, "no valid connection");
				return false;
			}

			if (!connection.isOpen()) {
				// FIXME maybe should not remove here, just using pushmanager.stateBuilder for this
				this.RemoveConnection(connection);
				if (this.logger.isInfoEnabled())
					this.logger.info("connection#%s[%s] is closed, remove it",
							connection.getId(),
							connection.getOrigin());
				continue;
			}

			SendStatus status;
			try {
				// here maybe have many exception, use statusCode instead
				status = connection.sendMessage(message);
			} catch (Exception e) {
				// exception maybe any kind of course, just contine
				this.logger.error(String.format("send message error to %s[%s]: %s",
						connection.getId(),
						connection.getOrigin(),
						message), e);
				continue;
			}

			switch (status) {
			case SENT:
				connectionQueue.add(connection);
				this.onSent(message);
				return true;
			case DROP:
				// if drop, means that the connection maybe have some problem,
				// and not want to be used in this flush loop,
				// but flush loop still can be continued without using this dropped connection
				return true;
			case RETRY:
				continue;
			}
		}
	}

	protected void markAsOffline() {
		if (this.clientStateHandler != null)
			this.clientStateHandler.onClientOffline(this);
	}

	protected void markAsIdle() {
		if (this.clientStateHandler != null)
			this.clientStateHandler.onClientIdle(this);
	}

	protected void markAsPending() {
		if (this.clientStateHandler != null)
			this.clientStateHandler.onClientPending(this);
	}

	private void onDrop(Object message, String reason) {
		if (this.messageStateHandler != null)
			this.messageStateHandler.onDropped(this.id, message, reason);
	}

	private void onSent(Object message) {
		if (this.messageStateHandler != null)
			this.messageStateHandler.onSent(this.id, message);
	}

	private void onConnect(ClientConnection connection) {
		if (this.clientStateHandler != null)
			this.clientStateHandler.onClientConnect(this, connection);
	}

	private void onDisconnect(ClientConnection connection) {
		if (this.clientStateHandler != null)
			this.clientStateHandler.onClientDisconnect(this, connection);
	}
}
