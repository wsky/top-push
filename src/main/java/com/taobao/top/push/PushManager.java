package com.taobao.top.push;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PushManager {
	private LoggerFactory loggerFactory;
	private Logger logger;

	private Object clientLock = new Object();
	private int maxConnectionCount;

	// all connections whatever from any client
	private int totalConnections;
	private int totalPendingMessages;
	// easy find client by id
	private HashMap<Identity, Client> clients;
	// hold clients which having pending messages and in processing
	// not immediately
	private ConcurrentLinkedQueue<Client> pendingClients;
	// hold clients which do not having pending messages and not in processing
	// not immediately
	private LinkedHashMap<Identity, Client> idleClients;
	// hold clients which do not having any active connections
	// not immediately
	private LinkedHashMap<Identity, Client> offlineClients;

	private HashMap<Sender, Thread> senders;
	// for managing some worker state
	private CancellationToken token;

	private boolean stateBuilding;
	private Object stateBuildingLock = new Object();
	private ClientStateHandler clientStateHandler;
	private MessageStateHandler messageStateHandler;

	public PushManager(LoggerFactory loggerFactory,
			int maxConnectionCount,
			int senderCount,
			int senderIdle,
			int stateBuilderIdle) {
		this.loggerFactory = loggerFactory;
		this.logger = this.loggerFactory.create(this);

		this.maxConnectionCount = maxConnectionCount;

		this.clients = new HashMap<Identity, Client>(1000);
		this.pendingClients = new ConcurrentLinkedQueue<Client>();
		this.idleClients = new LinkedHashMap<Identity, Client>();
		this.offlineClients = new LinkedHashMap<Identity, Client>();

		// TODO:move to start and support start/stop/restart
		this.token = new CancellationToken();
		this.prepareSenders(senderCount, senderIdle);
		this.prepareChecker(stateBuilderIdle);
	}

	public void setClientStateHandler(ClientStateHandler handler) {
		this.clientStateHandler = handler;
	}

	public void setMessageStateHandler(MessageStateHandler messageStateHandler) {
		this.messageStateHandler = messageStateHandler;
	}

	// cancel all current job
	public void cancelAll() {
		this.token.setCancelling(true);
	}

	// resume job after cancelAll called
	public void resume() {
		this.token.setCancelling(false);
	}

	public Client getClient(Identity id) {
		return this.clients.get(id);
	}

	public boolean isReachMaxConnectionCount() {
		return this.totalConnections >= this.maxConnectionCount;
	}

	public boolean isIdleClient(Identity id) {
		return this.idleClients.containsKey(id);
	}

	public boolean isOfflineClient(Identity id) {
		return this.offlineClients.containsKey(id);
	}

	public Client pollPendingClient() {
		return this.pendingClients.poll();
	}

	public int getPendingClientCount() {
		// size() is O(n)
		// TODO:just change to list
		return this.pendingClients.size();
	}

	public Client connectClient(Map<String, String> headers,
			ClientConnection clientConnection) throws Exception {
		Identity id = this.clientStateHandler != null ?
				this.clientStateHandler.onClientConnecting(headers) :
				new DefaultIdentity(headers.get("id"));
		clientConnection.init(id, headers);
		Client client = this.getOrCreateClient(id);
		client.AddConnection(clientConnection);
		return client;
	}

	public void disconnectClient(Client client, ClientConnection clientConnection) {
		if (this.clientStateHandler != null)
			this.clientStateHandler.onClientDisconnect(client, clientConnection);

		client.RemoveConnection(clientConnection);
		clientConnection.clear();
	}

	private Client getOrCreateClient(Identity id) {
		if (!this.clients.containsKey(id)) {
			synchronized (this.clientLock) {
				if (!this.clients.containsKey(id))
					this.clients.put(id, new Client(
							this.loggerFactory, id,
							this.messageStateHandler,
							this.clientStateHandler));
			}
		}
		return this.clients.get(id);
	}

	private void prepareSenders(int senderCount, int senderIdle) {
		this.senders = new HashMap<Sender, Thread>();
		for (int i = 0; i < senderCount; i++) {
			Sender sender = new Sender(
					this.loggerFactory, this, this.token, senderIdle, senderCount);
			Thread thread = new Thread(sender);
			thread.start();
			this.senders.put(sender, thread);
		}
	}

	private void prepareChecker(int stateBuilderIdle) {
		// timer check
		TimerTask task = new TimerTask() {
			public void run() {
				if (stateBuilding)
					return;

				synchronized (stateBuildingLock) {
					if (stateBuilding)
						return;
					stateBuilding = true;
				}

				// checking senders
				try {
					for (Map.Entry<Sender, Thread> entry : senders.entrySet()) {
						if (!entry.getValue().isAlive())
							logger.warn("sender#%s is broken!", entry.getKey());
					}
				} catch (Exception e) {
					logger.error(e);
				}
				try {
					rebuildClientsState();
					if (logger.isDebugEnabled())
						logger.debug(
								"total %s pending messages, total %s connections, total %s clients, %s is idle, %s is offline",
								totalPendingMessages,
								totalConnections,
								clients.size(),
								idleClients.size(),
								offlineClients.size());

				} catch (Exception e) {
					logger.fatal("rebuildClientsState error!", e);
				}

				stateBuilding = false;
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

			try {
				this.rebuildClientsState(client, noPending, pending, offline);
			} catch (Exception e) {
				this.logger.error(String.format(
						"error on rebuilding client#%s state", client.getId()), e);
			}
		}
		this.totalConnections = totalConn;
		this.totalPendingMessages = totalPending;
	}

	private void rebuildClientsState(Client client, boolean noPending,
			boolean pending, boolean offline) {
		if (noPending && pending && !offline) {
			this.pendingClients.add(client);
			this.idleClients.remove(client.getId());
			this.offlineClients.remove(client.getId());
			if (this.clientStateHandler != null)
				this.clientStateHandler.onClientPending(client);
		} else if (!pending && !offline) {
			this.idleClients.put(client.getId(), client);
			this.offlineClients.remove(client.getId());
			if (this.clientStateHandler != null)
				this.clientStateHandler.onClientIdle(client);
		} else if (offline) {
			this.offlineClients.put(client.getId(), client);
			this.idleClients.remove(client.getId());
			if (this.clientStateHandler != null)
				// can clear pending messages of offline client in this handler
				// after a long time
				// client.clearPendingMessages();
				this.clientStateHandler.onClientOffline(client);
		}
	}
}