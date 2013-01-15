package com.tmall.top.push;

import java.util.Date;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.tmall.top.push.messages.Message;

public class Client {
	private final static int MAX_FLUSH_COUNT = 100000;
	private String id;
	// ping from any connection
	private Date lastPingTime;
	private int totalSendMessageCount;

	private LinkedList<ClientConnection> connections;
	private ConcurrentLinkedQueue<ClientConnection> connectionQueue;
	private ConcurrentLinkedQueue<Message> pendingMessages;

	private PushManager manager;

	public Client(String id, PushManager manager) {
		this.id = id;
		this.receivePing();
		this.connections = new LinkedList<ClientConnection>();
		this.connectionQueue = new ConcurrentLinkedQueue<ClientConnection>();
		this.pendingMessages = new ConcurrentLinkedQueue<Message>();
		this.manager = manager;
	}

	public String getId() {
		return this.id;
	}

	public int getPendingMessagesCount() {
		return this.pendingMessages.size();
	}

	public int getConnectionsCount() {
		return this.connections.size();
	}

	public int getConnectionQueueSize() {
		return this.connectionQueue.size();
	}

	public Date getLastPingTime() {
		return this.lastPingTime;
	}

	public void AddConnection(ClientConnection conn) {
		synchronized (this.connections) {
			this.connections.add(conn);
		}
		this.connectionQueue.add(conn);
		System.out.println(String.format(
				"client#%s add new connection from %s", this.getId(),
				conn.getOrigin()));
	}

	public void RemoveConnection(ClientConnection conn) {
		synchronized (this.connections) {
			this.connections.remove(conn);
		}
		this.connectionQueue.remove(conn);
		System.out.println(String.format(
				"client#%s remove a connection from %s", this.getId(),
				conn.getOrigin()));
	}

	public void receivePing() {
		this.lastPingTime = new Date();
	}

	// pend message waiting to be send
	public void pendingMessage(Message msg) {
		if (msg != null)
			this.pendingMessages.add(msg);
	}
	
	public void clearPendingMessages() {
		this.pendingMessages.clear();
	}

	public void flush(CancellationToken token, int count) {
		int temp = 0;
		for (int i = 0; i < count; i++) {
			if (token.isCancelling())
				break;
			// prevent client and bandwidth usage
			if (i == MAX_FLUSH_COUNT - 1)
				break;
			Message msg = this.pendingMessages.poll();
			if (msg == null)
				break;
			this.SendMessage(token, msg);
			temp++;
		}
		this.totalSendMessageCount += temp;
		if (temp > 0)
			System.out.println(String.format(
					"flush %s messages to client#%s, totalSendMessageCount=%s",
					temp, this.getId(), this.totalSendMessageCount));
	}

	private void SendMessage(CancellationToken token, Message msg) {
		// FIFO queue for LRU load-balance
		while (true) {
			if (token.isCancelling())
				break;
			ClientConnection connection = this.connectionQueue.poll();
			if (connection == null)
				break;
			if (!connection.isOpen()) {
				// TODO:release connection object here? or websocketbase always
				// do this?
				System.out.println(String.format(
						"connection#%s[%s] is closed, remove it",
						connection.getId(), connection.getOrigin()));
				continue;
			}
			try {
				connection.sendMessage(msg);
			} catch (Exception e) {
				System.out.println("send message error");
				e.printStackTrace();
			} finally {
				this.connectionQueue.add(connection);
			}
			// only send once whatever exception occur
			break;
		}
		// release message object always
		this.manager.getReceiver().release(msg);
	}
}
