package com.taobao.top.push;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Client {
	private Logger logger;
	private Object id;
	private Map<Object, Object> context;
	private List<ClientConnection> connections;

	private MessagingScheduler scheduler;
	private MessageSender sender;

	private int pendingCount;

	public Client(Object id) {
		this.logger = LoggerFactory.getLogger(this.getClass());
		this.id = id;
		this.context = new HashMap<Object, Object>();
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

	public Map<Object, Object> getContext() {
		return new HashMap<Object, Object>(this.context);
	}

	public Object getContext(Object key) {
		return this.context.get(key);
	}

	public void setContext(Object key, Object value) {
		this.context.put(key, value);
	}

	public void setScheduler(MessagingScheduler scheduler) {
		this.scheduler = scheduler;
	}

	public void send(final Object message, boolean ordering, final MessagingHandler handler) {
		this.pendingCount++;

		if (!ordering) {
			this.scheduler.schedule(this, new MessagingTask() {
				@Override
				public MessagingStatus execute() {
					pendingCount--;

					try {
						if (!handler.preSend())
							return MessagingStatus.ABORT;
						MessagingStatus status = sender.send(message);
						handler.postSend(status);
						return status;
					} catch (Exception e) {
						handler.exceptionCaught(e);
						return MessagingStatus.FAULT;
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

	public int getPendingMessageCount() {
		return this.pendingCount;
	}

	public int getConnectionCount() {
		return this.connections.size();
	}

	public int getValidConnectionCount() {
		int c = 0;
		try {
			int size = this.connections.size();
			for (int i = 0; i < size; i++) {
				if (this.connections.get(i).isValid())
					c++;
			}
		} catch (Exception e) {
			this.logger.warn("count error", e);
		}
		return c;
	}

	public ClientConnection[] getConnections() {
		return this.connections.toArray(new ClientConnection[this.connections.size()]);
	}

	public void addConnection(ClientConnection connection) {
		synchronized (this.connections) {
			connection.setClientId(this.getId());
			this.connections.add(connection);
			this.sender.setConnections(this.getConnections());
		}
	}

	public void removeConnection(ClientConnection connection) {
		synchronized (this.connections) {
			this.connections.remove(connection);
			this.sender.setConnections(this.getConnections());
		}
	}
}
