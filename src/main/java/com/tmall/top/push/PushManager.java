package com.tmall.top.push;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.servlet.ServletConfig;

public final class PushManager {
	// TODO: use IOC managing life cycle
	private static Object lock = new Object();
	private static PushManager current;

	public static void Init(ServletConfig config) {
		// TODO:read config from ServletConfig
		current = new PushManager(100000, 1024, 10, 100000, 100000);
	}

	public static PushManager Current() {
		return current;
	}

	private WebSocketClientConnectionPool clientConnectionPool;
	private ConcurrentHashMap<String, Client> clients;
	private Receiver receiver;
	private List<Sender> senders;

	public PushManager(int connPoolSize, int publishMessageSize,
			int confirmMessageSize, int publishMessageBufferCount,
			int confirmMessageBufferCount) {
		this.clientConnectionPool = new WebSocketClientConnectionPool(
				connPoolSize);
		this.clients = new ConcurrentHashMap<String, Client>();
		this.receiver = new Receiver(publishMessageSize, confirmMessageSize,
				publishMessageBufferCount, confirmMessageBufferCount);
		// TODO:start sender workers
	}

	public WebSocketClientConnectionPool getClientConnectionPool() {
		return this.clientConnectionPool;
	}

	public Client getClient(String id) {
		if (!clients.containsKey(id)) {
			synchronized (lock) {
				if (!clients.containsKey(id))
					clients.put(id, new Client(id));
			}
		}
		return clients.get(id);
	}

	public Receiver getReceiver() {
		return this.receiver;
	}
}
