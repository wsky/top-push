package com.tmall.top.push;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletConfig;

public final class PushManager {
	private final static String CONFIRM_CLIENT = "confirm";
	// TODO: use IOC managing life cycle
	private static Object lock = new Object();
	private static PushManager current;

	public static void Init(ServletConfig config) {
		// TODO:read config from ServletConfig
		current = new PushManager(100000, 1024, 10, 100000, 100000, 4, 1);
	}

	public static PushManager Current() {
		return current;
	}

	private WebSocketClientConnectionPool clientConnectionPool;
	private ConcurrentHashMap<String, Client> clients;
	private Receiver receiver;
	private HashMap<Sender, Thread> senders;
	private CancellationToken token;

	public PushManager(int connPoolSize, int publishMessageSize,
			int confirmMessageSize, int publishMessageBufferCount,
			int confirmMessageBufferCount, int publishSenderCount,
			int confirmSenderCount) {
		this.clientConnectionPool = new WebSocketClientConnectionPool(
				connPoolSize);
		this.clients = new ConcurrentHashMap<String, Client>();
		this.receiver = new Receiver(publishMessageSize, confirmMessageSize,
				publishMessageBufferCount, confirmMessageBufferCount);

		this.prepareSenders(publishSenderCount, confirmSenderCount);
		this.prepareChecker();
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

	public Client getConfirmClient() {
		return this.getClient(CONFIRM_CLIENT);
	}

	public Receiver getReceiver() {
		return this.receiver;
	}

	private void prepareSenders(int publishSenderCount, int confirmSenderCount) {
		this.token = new CancellationToken();
		senders = new HashMap<Sender, Thread>();
		for (int i = 0; i < publishSenderCount; i++) {
			PublishSender sender = new PublishSender(token, this);
			Thread thread = new Thread(sender);
			thread.start();
			senders.put(sender, thread);
		}
		for (int i = 0; i < confirmSenderCount; i++) {
			ConfirmSender sender = new ConfirmSender(token, this);
			Thread thread = new Thread(sender);
			thread.start();
			senders.put(sender, thread);
		}
	}

	private void prepareChecker() {
		// timer check
		TimerTask task = new TimerTask() {
			public void run() {
				try {
					System.out.println("checking sender working well");
					for (Map.Entry<Sender, Thread> entry : senders.entrySet()) {
						if (!entry.getValue().isAlive())
							System.out.println(String.format(
									"sender#%s is broken!", entry.getKey()));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		Timer timer = new Timer(true);
		timer.schedule(task, new Date(), 10000);
	}
}
