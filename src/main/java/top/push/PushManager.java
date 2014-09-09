package top.push;

import java.util.Map;
import java.util.TreeMap;

// just a simple client push manager
public class PushManager {
	protected Map<Object, Client> clients;
	protected MessagingScheduler scheduler;

	public PushManager() {
		this.clients = new TreeMap<Object, Client>();
	}

	public void setMessagingScheduler(MessagingScheduler scheduler) {
		this.scheduler = scheduler;
	}

	public Client getClient(Object id) {
		return this.clients.get(id);
	}

	public Client[] getClients() {
		return this.clients.values().toArray(new Client[this.clients.size()]);
	}

	public Client getOrCreateClient(Object id) {
		if (!this.clients.containsKey(id)) {
			synchronized (this.clients) {
				if (!this.clients.containsKey(id))
					this.clients.put(id, this.createClient(id, this.scheduler));
			}
		}
		return this.clients.get(id);
	}

	protected Client createClient(Object id, MessagingScheduler scheduler) {
		return new Client(id, scheduler);
	}
}