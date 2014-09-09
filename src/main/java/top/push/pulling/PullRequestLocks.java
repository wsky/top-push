package top.push.pulling;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import top.push.Client;

public class PullRequestLocks {
	private int timeout = 1000 * 10;
	protected String contextKey = "locks";
	
	public void setTimeout(int value) {
		this.timeout = value;
	}
	
	public boolean acquire(Client client, Object request) {
		return this.acquireAndGet(client, request) != null;
	}
	
	public Lock acquireAndGet(Client client, Object request) {
		Lock lock = this.getLock(client, request);
		
		if (lock == null) {
			synchronized (client) {
				if (this.getLock(client, request) == null)
					this.setLock(client, request, lock = this.createLock());
			}
		}
		
		if (this.isLocked(lock))
			return null;
		
		synchronized (lock) {
			if (this.isLocked(lock))
				return null;
			
			this.lock(lock);
			return lock;
		}
	}
	
	public void release(Client client, Object request) {
		this.getLock(client, request).unlock();
	}
	
	public void release(Client client) {
		Map<Object, Lock> locks = this.getLocks(client);
		for (Entry<Object, Lock> e : locks.entrySet())
			this.release(client, e.getKey());
	}
	
	private void lock(Lock lock) {
		lock.lock();
	}
	
	private boolean isLocked(Lock lock) {
		return lock.isLocked(this.timeout);
	}
	
	private Lock createLock() {
		return new Lock();
	}
	
	private Lock getLock(Client client, Object request) {
		return this.getLocks(client).get(request);
	}
	
	private void setLock(Client client, Object request, Lock lock) {
		this.getLocks(client).put(request, lock);
	}
	
	private Map<Object, Lock> getLocks(Client client) {
		Map<Object, Lock> locks = this.getLocksInContext(client);
		if (locks == null) {
			synchronized (client) {
				if ((locks = this.getLocksInContext(client)) == null)
					client.setContext(
							this.contextKey,
							locks = new HashMap<Object, Lock>());
			}
		}
		return locks;
	}
	
	@SuppressWarnings("unchecked")
	private Map<Object, Lock> getLocksInContext(Client client) {
		return (Map<Object, Lock>) client.getContext(this.contextKey);
	}
	
	public static class Lock {
		private boolean isLocked;
		private long lockedAt;
		
		public boolean isLocked(int timeout) {
			return this.isLocked && System.currentTimeMillis() - this.lockedAt < timeout;
		}
		
		public void lock() {
			this.isLocked = true;
			this.lockedAt = System.currentTimeMillis();
		}
		
		public void unlock() {
			this.isLocked = false;
		}
		
		@Override
		public String toString() {
			return String.format("%s-%s", this.isLocked, this.lockedAt);
		}
	}
}
