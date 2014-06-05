package com.taobao.top.push.pulling;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.taobao.top.push.Client;
import com.taobao.top.push.pulling.PullRequestLocks.Lock;

public abstract class ClientPullings extends PeriodTaskExecutor {
	protected static Logger logger = LoggerFactory.getLogger(ClientPullings.class);
	
	private volatile Set<Client> clients = this.newSet();
	
	private PullRequestLocks locks = new PullRequestLocks();
	private PullRequestScheduler scheduler;
	
	public void setLocks(PullRequestLocks locks) {
		this.locks = locks;
	}
	
	public void setScheduler(PullRequestScheduler scheduler) {
		this.scheduler = scheduler;
	}
	
	public void add(Client client) {
		this.clients.add(client);
	}
	
	@Override
	protected TimerTask createTask() {
		return new TimerTask() {
			@Override
			public void run() {
				try {
					dispatchAll();
				} catch (Exception e) {
					logger.error("dispatch error", e);
				}
			}
		};
	}
	
	protected void dispatchAll() {
		Set<Client> clients = this.clients;
		this.clients = this.newSet();
		
		for (Client c : clients) {
			try {
				this.dispatch(c);
			} catch (Exception e) {
				logger.error("dispatch error: " + c.getId(), e);
			}
		}
	}
	
	protected void dispatch(Client client) {
		Object r = this.getPullRequest(client);
		
		if (r instanceof List<?>) {
			List<?> requests = (List<?>) r;
			for (Object o : requests) {
				try {
					this.dispatch(client, o);
				} catch (Exception e) {
					logger.error("dispatch error: " + o, e);
				}
			}
		} else
			this.dispatch(client, r);
	}
	
	protected void dispatch(Client client, Object request) {
		Lock lock = this.locks.acquireAndGet(client, request);
		
		if (lock != null)
			this.scheduler.dispatch(client, request, lock);
	}
	
	protected Set<Client> newSet() {
		return Collections.newSetFromMap(new ConcurrentHashMap<Client, Boolean>());
	}
	
	protected abstract Object getPullRequest(Client client);
}
