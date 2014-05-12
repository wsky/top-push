package com.taobao.top.push;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PushManager {
	private static Logger logger = LoggerFactory.getLogger(PushManager.class);

	private Map<Object, Client> clients;
	private ClientStateHandler clientStateHandler;
	private MessagingScheduler scheduler;

	private Timer timer;
	private TimerTask timerTask;

	private int connectionCount;

	public PushManager() {
		this.clients = new TreeMap<Object, Client>();
		this.setStateBuilderPeriod(1000);
	}

	public void setClientStateHandler(ClientStateHandler handler) {
		this.clientStateHandler = handler;
	}

	public void setMessagingScheduler(MessagingScheduler scheduler) {
		this.scheduler = scheduler;
	}

	public void setStateBuilderPeriod(int period) {
		if (this.timerTask != null)
			this.timerTask.cancel();

		if (period <= 0)
			return;

		if (this.timer == null)
			this.timer = new Timer("state-builder", true);

		this.timer.purge();
		this.timer.schedule(this.timerTask = new TimerTask() {
			public void run() {
				try {
					rebuildState();
				} catch (Exception e) {
					error("rebuildState error", e);
				}
			}
		}, period, period);
	}

	public Client getClient(Object id) {
		return this.clients.get(id);
	}

	public Client[] getClients() {
		return this.clients.values().toArray(new Client[this.clients.size()]);
	}

	public int getConnectionCount() {
		return this.connectionCount;
	}

	public Client connectClient(Object id, ClientConnection connection) {
		Client client = this.getOrCreateClient(id);
		client.addConnection(connection);
		this.onConnect(client, connection);
		return client;
	}

	public void disconnectClient(Client client, ClientConnection connection, String reasonText) {
		client.removeConnection(connection);
		this.onDisconnect(client, connection, reasonText);
	}

	public void disconnectClient(Object id, String reasonText) {
		Client client = this.getClient(id);
		if (client == null)
			return;
		this.clients.remove(id);
		this.cleanConnections(client, true, reasonText);
	}

	protected void rebuildState() {
		boolean flag = false;
		do {
			Iterator<Entry<Object, Client>> iterator = this.clients.entrySet().iterator();
			while (iterator.hasNext()) {
				Client client = null;
				try {
					client = iterator.next().getValue();
				} catch (Exception e) {
					if (e instanceof ConcurrentModificationException)
						flag = true;
					break;
				}

				if (client == null)
					continue;

				try {
					int connCount = this.cleanConnections(client, false, "state");
					if (connCount == 0) {
						this.markAsOffline(client);
						continue;
					}

					if (client.getPendingMessageCount() == 0) {
						this.markAsIdle(client);
						continue;
					}

					this.markAsPending(client);
				} catch (Exception e) {
					this.error(String.format(
							"error on rebuilding client#%s state", client.getId()), e);
				}
			}
		} while (flag);
	}

	protected Client getOrCreateClient(Object id) {
		if (!this.clients.containsKey(id)) {
			synchronized (this.clients) {
				if (!this.clients.containsKey(id))
					this.clients.put(id, new Client(id, this.scheduler));
			}
		}
		return this.clients.get(id);
	}

	private int cleanConnections(Client client, boolean force, String reasonText) {
		ClientConnection[] connections = client.getConnections();
		for (ClientConnection c : connections) {
			if (force || !c.isOpen()) {
				client.removeConnection(c);
				this.onDisconnect(client, c, reasonText);
			}
		}
		return client.getConnectionCount();
	}

	private void markAsOffline(Client client) {
		if (this.clientStateHandler != null)
			this.clientStateHandler.onClientOffline(client);
	}

	private void markAsIdle(Client client) {
		if (this.clientStateHandler != null)
			this.clientStateHandler.onClientIdle(client);
	}

	private void markAsPending(Client client) {
		if (this.clientStateHandler != null)
			this.clientStateHandler.onClientPending(client);
	}

	private void onConnect(Client client, ClientConnection connection) {
		this.connectionCount++;
		if (this.clientStateHandler != null)
			this.clientStateHandler.onClientConnect(client, connection);
	}

	private void onDisconnect(Client client, ClientConnection connection, String reasonText) {
		this.connectionCount--;
		if (this.clientStateHandler != null)
			this.clientStateHandler.onClientDisconnect(client, connection, reasonText);
	}

	protected void error(String message, Exception e) {
		logger.error(message, e);
	}
}