package com.taobao.top.push.pulling;

import java.util.AbstractMap;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;

import com.taobao.top.push.Client;

public class PullRequestLocks {
	private int timeout = 1000 * 10;

	public void setTimeout(int value) {
		this.timeout = value;
	}

	public boolean acquire(Client client, Object request) {
		Entry<AtomicBoolean, Long> lock = this.getLock(client, request);

		if (lock == null) {
			synchronized (client) {
				if (this.getLock(client, request) == null)
					this.setLock(client, request, lock = this.createLock());

			}
		}

		if (this.isLocked(lock))
			return false;

		synchronized (lock) {
			if (this.isLocked(lock))
				return false;

			this.lock(lock);
			return true;
		}
	}

	public void release(Client client, Object request) {
		this.getLock(client, request).getKey().set(false);
	}

	private void lock(Entry<AtomicBoolean, Long> lock) {
		lock.getKey().set(true);
		lock.setValue(System.currentTimeMillis());
	}

	private boolean isLocked(Entry<AtomicBoolean, Long> lock) {
		return lock.getKey().get() && System.currentTimeMillis() - lock.getValue() < this.timeout;
	}

	private Entry<AtomicBoolean, Long> createLock() {
		return new AbstractMap.SimpleEntry<AtomicBoolean, Long>(new AtomicBoolean(), System.currentTimeMillis());
	}

	@SuppressWarnings("unchecked")
	private Entry<AtomicBoolean, Long> getLock(Client client, Object request) {
		return (Entry<AtomicBoolean, Long>) client.getContext(request);
	}

	private void setLock(Client client, Object request, Entry<AtomicBoolean, Long> lock) {
		client.setContext(request, lock);
	}
}
