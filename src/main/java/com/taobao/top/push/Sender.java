package com.taobao.top.push;

import java.util.concurrent.Semaphore;

public class Sender implements Runnable {
	private Logger logger;
	protected CancellationToken token;
	protected PushManager manager;
	private Client pendingClient;
	private Semaphore semaphore;

	private int highwater = 100000;
	private int maxFlushCount = 2000;
	private int minFlushCount = 100;

	public void setHighwater(int value) {
		this.highwater = value;
	}

	public void setMaxFlushCount(int value) {
		this.maxFlushCount = value;
	}

	public void setMinFlushCount(int value) {
		this.minFlushCount = value;
	}

	public CancellationToken getCancellationToken() {
		return this.token;
	}

	public Sender(LoggerFactory loggerFactory,
			PushManager manager,
			CancellationToken token,
			Semaphore semaphore) {
		this.logger = loggerFactory.create(this);
		this.manager = manager;
		this.token = token;
		this.semaphore = semaphore;
	}

	@Override
	public void run() {
		while (!this.token.isStoping()) {
			try {
				doSend();
			} catch (Exception e) {
				this.logger.error(e);
			}
			this.semaphore.acquireUninterruptibly();
		}
	}

	protected void doSend() {
		// TODO: auto adjust max flush count
		// https://github.com/wsky/top-push/issues/24
		// 100000 is max message count server can flush per second
		int pending = this.manager.getPendingClientCount();
		pending = pending == 0 ? 1 : pending;
		int flushCount = this.highwater / pending;
		if (flushCount > this.maxFlushCount)
			flushCount = this.maxFlushCount;
		if (flushCount < this.minFlushCount)
			flushCount = this.minFlushCount;

		while (!this.token.isCancelling()
				&& (this.pendingClient = this.manager.pollPendingClient()) != null) {
			this.pendingClient.flush(token, flushCount);
		}
	}
}
