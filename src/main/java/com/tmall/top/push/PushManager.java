package com.tmall.top.push;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.servlet.ServletConfig;
import com.tmall.top.push.messaging.ConfirmMessage;
import com.tmall.top.push.messaging.EventMessage;

public final class PushManager {
	// TODO: use IOC managing life cycle
	private static Object lock = new Object();
	private static PushManager current;

	public static void Init(ServletConfig config) {
		current = new PushManager(1024);
		current.receiver =new Receiver(100000, 100000);
		//start sender workers
	}

	public static PushManager Current() {
		return current;
	}

	// object pool
	public WebSocketClientConnectionPool ClientConnectionPool;

	public ConcurrentHashMap<String, Client> clients;

	public Receiver receiver;
	
	public PushManager(int connPoolSize) {
		this.ClientConnectionPool = new WebSocketClientConnectionPool(
				connPoolSize);
		this.clients = new ConcurrentHashMap<String, Client>();
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
}
