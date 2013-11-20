package com.taobao.top.push;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;

public class PushManager {
	private LoggerFactory loggerFactory;
	private Logger logger;

	private Object clientLock = new Object();
	private int maxConnectionCount;

	// all connections whatever from any client
	private int totalConnections;
	private long totalPendingMessages;
	// easy find client by id
	private Map<Object, Client> clients;
	// hold clients which having pending messages and in processing
	// not immediately
	private Queue<Client> pendingClients;
	// hold clients which do not having pending messages and not in processing
	// not immediately
	private Map<Object, Client> idleClients;
	// hold clients which do not having any active connections
	// not immediately
	private Map<Object, Client> offlineClients;

	private Thread sendWorker;
	private Sender sender;
	private Semaphore senderSemaphore;
	private int senderCount;
	// for managing some worker state
	private CancellationToken token;

	private boolean stateBuilding;
	private Object stateBuildingLock = new Object();
	private ClientStateHandler clientStateHandler;
	private MessageStateHandler messageStateHandler;

	public PushManager(LoggerFactory loggerFactory,
			int maxConnectionCount,
			int senderCount,
			int stateBuilderIdle) {
		this.loggerFactory = loggerFactory;
		this.logger = this.loggerFactory.create(this);

		this.maxConnectionCount = maxConnectionCount;

		this.clients = new ConcurrentHashMap<Object, Client>(1000);
		this.pendingClients = new ConcurrentLinkedQueue<Client>();
		this.idleClients = new LinkedHashMap<Object, Client>();
		this.offlineClients = new LinkedHashMap<Object, Client>();

		// TODO move to start and support start/stop/restart
		this.token = new CancellationToken();
		this.senderCount = senderCount;
		this.prepareSenders();
		this.prepareChecker(stateBuilderIdle);
	}

	public void setClientStateHandler(ClientStateHandler handler) {
		this.clientStateHandler = handler;
	}

	public void setMessageStateHandler(MessageStateHandler messageStateHandler) {
		this.messageStateHandler = messageStateHandler;
	}

	// sender settings

	public void setSenderHighWater(int value) {
		this.sender.setHighwater(value);
	}

	public void setSenderMaxFlushCount(int value) {
		this.sender.setMaxFlushCount(value);
	}

	public void setSenderMinFlushCount(int value) {
		this.sender.setMinFlushCount(value);
	}

	// cancel all current job
	public void cancelAll() {
		this.token.setCancelling(true);
	}

	// resume job after cancelAll called
	public void resume() {
		this.token.setCancelling(false);
	}

	public Client getClient(Object id) {
		return this.clients.get(id);
	}

	public Client[] getClients() {
		return this.clients.values().toArray(new Client[0]);
	}

	public boolean isReachMaxConnectionCount() {
		return this.totalConnections >= this.maxConnectionCount;
	}

	public boolean isIdleClient(Object id) {
		return this.idleClients.containsKey(id);
	}

	public boolean isOfflineClient(Object id) {
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

	public Client connectClient(Object id, ClientConnection clientConnection) {
		Client client = this.getOrCreateClient(id);
		client.AddConnection(clientConnection);
		return client;
	}

	public void disconnectClient(Client client, ClientConnection clientConnection) {
		client.RemoveConnection(clientConnection);
		clientConnection.clear();
	}

	public void disconnectClient(Object id, String reasonText) {
		Client client = this.getClient(id);
		if (client == null)
			return;
		java.util.logging.LogManager.getLogManager().getLogger("").log(null);
		client.disconnect(reasonText);
		this.clients.remove(id);
	}

	private Client getOrCreateClient(Object id) {
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

	protected void prepareSenders() {
		this.senderSemaphore = new Semaphore(0);
		this.sender = new Sender(this.loggerFactory,
				this.token,
				this.senderSemaphore,
				this.senderCount) {
			@Override
			protected int getPending() {
				return getPendingClientCount();
			}

			@Override
			protected Client pollPending() {
				return pollPendingClient();
			}
		};
		this.sendWorker = new Thread(this.sender);
		this.sendWorker.start();
	}

	protected void prepareChecker(int stateBuilderIdle) {
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
					if (!sendWorker.isAlive()) {
						logger.fatal("sender is broken! restarting...");
						prepareSenders();
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
	protected void rebuildClientsState() {
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

			connCount = client.cleanConnections();
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
		// tell sender work
		this.senderSemaphore.release();
	}

	protected void rebuildClientsState(Client client, boolean noPending,
			boolean pending, boolean offline) {
		if (noPending && pending && !offline) {
			this.pendingClients.add(client);
			this.idleClients.remove(client.getId());
			this.offlineClients.remove(client.getId());
			client.markAsPending();
		} else if (!pending && !offline) {
			this.idleClients.put(client.getId(), client);
			this.offlineClients.remove(client.getId());
			client.markAsIdle();
		} else if (offline) {
			this.offlineClients.put(client.getId(), client);
			this.idleClients.remove(client.getId());
			client.markAsOffline();
		}
	}
}