package com.taobao.top.push;

import java.util.ArrayList;
import java.util.List;

public class Client {
	private Object id;
	private List<ClientConnection> connections;

	private MessagingScheduler scheduler;
	private MessageSender sender;

	public Client(Object id) {
		this.id = id;
		this.connections = new ArrayList<ClientConnection>();
		this.sender = new ClientMessageSender();
	}

	public Client(Object id, MessagingScheduler scheduler) {
		this(id);
		this.setScheduler(scheduler);
	}

	public Object getId() {
		return this.id;
	}

	public void setScheduler(MessagingScheduler scheduler) {
		this.scheduler = scheduler;
	}

	public void send(final Object message, boolean ordering, final MessagingHandler handler) {
		if (!ordering) {
			this.scheduler.schedule(this, new Runnable() {
				@Override
				public void run() {
					try {
						if (handler.preSend())
							handler.postSend(sender.send(message));
					} catch (Exception e) {
						handler.exceptionCaught(e);
					}

				}
			});
		}
	}

	public MessageSender newSender() {
		// TODO manage all senders
		MessageSender sender = new BatchedMessageSender();
		sender.setConnections(this.getConnections());
		return sender;
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
		this.sender.setConnections(this.getConnections());
	}

	protected synchronized void removeConnection(ClientConnection connection) {
		this.connections.remove(connection);
		this.sender.setConnections(this.getConnections());
	}
}
