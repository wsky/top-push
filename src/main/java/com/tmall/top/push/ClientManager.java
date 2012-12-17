package com.tmall.top.push;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.servlet.ServletConfig;
import com.tmall.top.push.messaging.ConfirmMessage;
import com.tmall.top.push.messaging.EventMessage;

public final class ClientManager {
	// TODO: use IOC managing life cycle
	private static Object lock = new Object();
	private static ClientManager current;

	public static void Init(ServletConfig config) {
		current = new ClientManager(1024, 100000, 100000);
	}

	public static ClientManager Current() {
		return current;
	}

	// object pool
	public WebSocketClientConnectionPool ClientConnectionPool;
	public EventMessagePool EventMessagePool;
	public ConfirmMessagePool ConfirmMessagePool;

	public ConcurrentHashMap<String, Client> clients;
	// TODO:choose a ring-buffer impl
	public ConcurrentLinkedQueue<ConfirmMessage> Confirms;
	public ConcurrentLinkedQueue<EventMessage> Events;

	public ClientManager(int connPoolSize, int eventMessagePoolSize,
			int confirmMessagePoolSize) {

		this.ClientConnectionPool = new WebSocketClientConnectionPool(
				connPoolSize);
		this.EventMessagePool = new EventMessagePool(eventMessagePoolSize);
		this.ConfirmMessagePool = new ConfirmMessagePool(confirmMessagePoolSize);

		this.clients = new ConcurrentHashMap<String, Client>();
		this.Confirms = new ConcurrentLinkedQueue<ConfirmMessage>();
		this.Events = new ConcurrentLinkedQueue<EventMessage>();
	}

	public synchronized Client getClient(String id) {
		if (!clients.containsKey(id)) {
			synchronized (lock) {
				if (!clients.containsKey(id))
					clients.put(id, new Client(id));
			}
		}
		return clients.get(id);
	}
}
