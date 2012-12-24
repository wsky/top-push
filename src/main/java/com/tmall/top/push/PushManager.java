package com.tmall.top.push;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PushManager {
	private static PushManager current;

	// TODO: use IOC managing life cycle
	public static void current(PushManager manager) {
		if (current == null)
			current = manager;
	}

	public static PushManager current() {
		return current;
	}

	private Object clientLock = new Object();
	private int maxConnectionCount;

	// all connections whatever from any client
	private int totalConnections;
	private int totalPendingMessages;
	// easy find client by id
	private HashMap<String, Client> clients;
	// hold clients which having pending messages and in processing
	// not immediately
	private ConcurrentLinkedQueue<Client> pendingClients;
	// hold clients which do not having pending messages and not in processing
	// not immediately
	private LinkedHashMap<String, Client> idleClients;
	// hold clients which do not having any active connections
	// not immediately
	private LinkedHashMap<String, Client> offlineClients;

	private Receiver receiver;
	private HashMap<Sender, Thread> senders;
	// for managing some worker state
	private CancellationToken token;

	public PushManager(int maxConnectionCount, int publishMessageSize,
			int confirmMessageSize, int publishMessageBufferCount,
			int confirmMessageBufferCount, int senderCount, int senderIdle,
			int stateBuilderIdle) {
		this.maxConnectionCount = maxConnectionCount;

		// client management
		this.clients = new HashMap<String, Client>(1000);
		this.pendingClients = new ConcurrentLinkedQueue<Client>();
		this.idleClients = new LinkedHashMap<String, Client>();
		this.offlineClients = new LinkedHashMap<String, Client>();

		this.receiver = new Receiver(publishMessageSize, confirmMessageSize,
				publishMessageBufferCount, confirmMessageBufferCount);

		// TODO:move to start and support start/stop/restart
		this.token = new CancellationToken();
		this.prepareSenders(senderCount, senderIdle);
		this.prepareChecker(stateBuilderIdle);
	}

	// cancel all current job
	public void cancelAll() {
		this.token.setCancelling(true);
	}

	// resume job after cancelAll called
	public void resume() {
		this.token.setCancelling(false);
	}

	public Receiver getReceiver() {
		return this.receiver;
	}

	public Client getClient(String id) {
		if (!this.clients.containsKey(id)) {
			synchronized (this.clientLock) {
				if (!this.clients.containsKey(id))
					this.clients.put(id, new Client(id, this));
			}
		}
		return this.clients.get(id);
	}

	public boolean isIdleClient(String id) {
		return this.idleClients.containsKey(id);
	}

	public boolean isOfflineClient(String id) {
		return this.offlineClients.containsKey(id);
	}

	public boolean isOnlineClient(String id) {
		return this.getClient(id).getConnectionsCount() > 0;
	}

	public boolean isReachMaxConnectionCount() {
		return this.totalConnections >= this.maxConnectionCount;
	}

	public Client pollPendingClient() {
		return this.pendingClients.poll();
	}

	private void prepareSenders(int senderCount, int senderIdle) {
		this.senders = new HashMap<Sender, Thread>();
		for (int i = 0; i < senderCount; i++) {
			Sender sender = new Sender(this, this.token, senderIdle);
			Thread thread = new Thread(sender);
			thread.start();
			this.senders.put(sender, thread);
		}
	}

	private void prepareChecker(int stateBuilderIdle) {
		// timer check
		TimerTask task = new TimerTask() {
			public void run() {
				// checking senders
				try {
					for (Map.Entry<Sender, Thread> entry : senders.entrySet()) {
						if (!entry.getValue().isAlive())
							System.out.println(String.format(
									"sender#%s is broken!", entry.getKey()));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				try {
					rebuildClientsState();
					System.out.println(String.format(
							"total %s pending messages, "
									+ "total %s connections, "
									+ "total %s clients, "
									+ "%s is idle, %s is offline",
							totalPendingMessages, totalConnections,
							clients.size(), idleClients.size(),
							offlineClients.size()));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		Timer timer = new Timer(true);
		timer.schedule(task, new Date(), stateBuilderIdle);
	}

	// build pending/idle clients queue
	private void rebuildClientsState() {
		int totalConn = 0;
		int totalPending = 0;
		int connCount, pendingCount;
		// still have pending clients in processing
		boolean noPending = this.pendingClients.isEmpty();
		boolean offline, pending;

		Object[] keys = null;
		// is there a better way? avoid array create
		synchronized (this.clientLock) {
			keys = this.clients.keySet().toArray();
		}
		for (int i = 0; i < keys.length; i++) {
			Client client = this.clients.get(keys[i]);
			if (client == null)
				continue;

			connCount = client.getConnectionsCount();
			pendingCount = client.getPendingMessagesCount();

			totalConn += connCount;
			totalPending += pendingCount;

			offline = connCount == 0;
			pending = pendingCount > 0;

			if (noPending && pending && !offline) {
				this.pendingClients.add(client);
				this.idleClients.remove(client.getId());
				this.offlineClients.remove(client.getId());
			} else if (!pending && !offline) {
				this.idleClients.put(client.getId(), client);
				this.offlineClients.remove(client.getId());
			} else if (offline) {
				// TODO:clear pending messages of offline client after
				// a long time
				this.offlineClients.put(client.getId(), client);
				this.idleClients.remove(client.getId());
			}
		}
		this.totalConnections = totalConn;
		this.totalPendingMessages = totalPending;
	}
}
