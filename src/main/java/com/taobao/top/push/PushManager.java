package com.taobao.top.push;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

public class PushManager {
	private LoggerFactory loggerFactory;
	private Logger logger;

	private Object clientLock = new Object();
	private int maxConnectionCount = 10000;

	// all connections whatever from any client
	private int totalConnectionCount;
	// easy find client by id
	private Map<Object, Client> clients;

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
			int senderCount,
			int stateBuilderIdle) {
		this.loggerFactory = loggerFactory;
		this.logger = this.loggerFactory.create(this);
		this.clients = new ConcurrentHashMap<Object, Client>(1000);
		// TODO move to start and support start/stop/restart
		this.token = new CancellationToken();
		this.senderCount = senderCount;
		this.prepareSenders();
		this.prepareChecker(stateBuilderIdle);
	}

	public void setMaxConnectionCount(int value) {
		this.maxConnectionCount = value;
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

	public void setSenderBalancing(boolean value) {
		this.sender.setBalancing(value);
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
		return this.totalConnectionCount >= this.maxConnectionCount;
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
		this.sender = new Sender(this.loggerFactory, this.token, this.senderSemaphore, this.senderCount);
		this.sendWorker = new Thread(this.sender);
		this.sendWorker.setDaemon(true);
		this.sendWorker.setName("push-sender");
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
					senderSemaphore.release();
				} catch (Exception e) {
					logger.fatal("rebuildClientsState error!", e);
				}

				stateBuilding = false;
			}
		};
		Timer timer = new Timer(true);
		timer.schedule(task, 0, stateBuilderIdle);
	}

	// build pending/idle clients queue
	protected void rebuildClientsState() {
		int total = 0;
		boolean flag = false;
		do {
			Iterator<Entry<Object, Client>> iterator = this.clients.entrySet().iterator();
			while (iterator.hasNext()) {
				Client client = null;
				try {
					client = iterator.next().getValue();
				} catch (Exception e) {
					if (this.logger.isDebugEnabled())
						this.logger.debug(e);
					if (e instanceof ConcurrentModificationException)
						flag = true;
					break;
				}

				if (client == null)
					continue;

				try {
					int connCount = client.cleanConnections();
					if (connCount == 0) {
						client.markAsOffline();
						continue;
					}
					total += connCount;

					if (client.getPendingMessagesCount() == 0) {
						client.markAsIdle();
						continue;
					}

					client.markAsPending();
					this.sender.pendingClient(client);
				} catch (Exception e) {
					this.logger.error(String.format(
							"error on rebuilding client#%s state", client.getId()), e);
				}
			}
		} while (flag);
		this.totalConnectionCount = total;
	}
}