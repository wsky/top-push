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
	private MessageSender sender;

	private Random rd = new Random();

	public Client(Object id, MessageSender sender) {
		this.id = id;
		this.sender = sender;
		this.connections = new ArrayList<ClientConnection>();
	}

	public Object getId() {
		return this.id;
	}

	public void send(Object message, boolean ordering, MessageCallback callback) {
		this.sender.send(message);
	}

	public ClientMessageSender newSender() {
		return new ClientMessageSender(this.createQueue());
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
