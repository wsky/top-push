package com.taobao.top.push;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public abstract class Sender implements Runnable {
	private Logger logger;
	private CancellationToken token;
	private Semaphore semaphore;
	private ExecutorService threadPool;

	// 100000 is max message count server can flush per second
	private int highwater = 100000;
	private int maxFlushCount = 2000;
	private int minFlushCount = 100;

	public Sender(LoggerFactory loggerFactory,
			CancellationToken token,
			Semaphore semaphore,
			int senderCount) {
		this.logger = loggerFactory.create(this);
		this.token = token;
		this.semaphore = semaphore;

		if (senderCount > 0)
			this.setThreadPool(new ThreadPoolExecutor(
					senderCount, senderCount,
					300, TimeUnit.SECONDS,
					new SynchronousQueue<Runnable>()));
	}

	public void setHighwater(int value) {
		this.highwater = value;
	}

	public void setMaxFlushCount(int value) {
		this.maxFlushCount = value;
	}

	public void setMinFlushCount(int value) {
		this.minFlushCount = value;
	}

	public void setThreadPool(ExecutorService threadPool) {
		this.threadPool = threadPool;
	}

	@Override
	public void run() {
		while (!this.token.isStoping()) {
			try {
				this.semaphore.tryAcquire(2, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				if (this.logger.isWarnEnabled())
					this.logger.warn(e);
			}

			if (this.threadPool == null)
				continue;

			int pending = this.getPending();
			if (pending == 0)
				continue;

			int flushCount = this.calculate(pending);
			Client pendingClient = null;

			while (!this.token.isCancelling()) {
				if (pendingClient == null) {
					try {
						pendingClient = this.pollPending();
					} catch (Exception e) {
						this.logger.error(e);
						break;
					}
				}

				if (pendingClient == null)
					break;

				try {
					this.threadPool.execute(this.createRunnable(pendingClient, flushCount));
					pendingClient = null;
				} catch (RejectedExecutionException e) {
					if (this.logger.isDebugEnabled())
						this.logger.debug(e);
					// if pool is full, retry later
					try {
						Thread.sleep(10);
					} catch (InterruptedException e1) {
					}
				} catch (Exception e) {
					this.logger.error(e);
					break;
				}
			}
		}

		this.logger.info("sender stop");
	}

	protected abstract int getPending();

	protected abstract Client pollPending();

	// https://github.com/wsky/top-push/issues/24
	protected int calculate(int pending) {
		int flushCount = this.highwater / pending;
		if (flushCount > this.maxFlushCount)
			flushCount = this.maxFlushCount;
		if (flushCount < this.minFlushCount)
			flushCount = this.minFlushCount;
		return flushCount;
	}

	protected Runnable createRunnable(final Client pendingClient, final int flushCount) {
		return new Runnable() {
			@Override
			public void run() {
				try {
					pendingClient.flush(token, flushCount);
				} catch (Exception e) {
					logger.error(e);
				}
			}
		};
	}
}
