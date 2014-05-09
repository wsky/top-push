package com.taobao.top.push;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Client {
	private Object id;
	private List<ClientConnection> connections;

	private MessagingScheduler scheduler;
	private MessageSender sender;

	private Random rd = new Random();

	public Client(Object id, MessagingScheduler scheduler) {
		this.id = id;
		this.scheduler = scheduler;
		this.connections = new ArrayList<ClientConnection>();
		this.sender = new ClientMessageSender(this.connections);
	}

	public Object getId() {
		return this.id;
	}

	public void send(final Object message, boolean ordering, MessageCallback callback) {
		if (!ordering) {
			this.scheduler.schedule(this, new Runnable() {
				@Override
				public void run() {
					sender.send(message);
				}
			});
		}
	}

	public MessageSender newSender() {
		return new BatchedMessageSender(this.createQueue());
	}

	public int getPendingMessagesCount() {
		// FIXME pending count
		return 0;
	}

	public int getConnectionsCount() {
		return this.connections.size();
	}

	public ClientConnection[] getConnections() {
		return this.connections.toArray(new ClientConnection[this.connections.size()]);
	}

	protected synchronized void addConnection(ClientConnection connection) {
		connection.setClientId(this.getId());
		this.connections.add(connection);
	}

	protected synchronized void removeConnection(ClientConnection connection) {
		this.connections.remove(connection);
	}

	private Queue<ClientConnection> createQueue() {
		List<ClientConnection> list = Arrays.asList(this.connections.toArray(new ClientConnection[this.connections.size()]));
		Collections.shuffle(list, this.rd);
		return new ConcurrentLinkedQueue<ClientConnection>(list);
	}
}
