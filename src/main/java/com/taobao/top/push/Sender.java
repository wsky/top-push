package com.taobao.top.push;

public class Sender implements Runnable {
	protected CancellationToken token;
	protected PushManager manager;
	private Client pendingClient;
	private int idle;

	public CancellationToken getCancellationToken() {
		return this.token;
	}

	public Sender(PushManager manager, CancellationToken token, int idle, int totalSenderCount) {
		this.manager = manager;
		this.token = token;
		this.idle = idle;
	}

	@Override
	public void run() {
		while (!this.token.isStoping()) {
			try {
				doSend();
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				Thread.sleep(this.idle);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	protected void doSend() {
		// TODO: auto adjust max flush count
		// https://github.com/wsky/top-push/issues/24
		// 100000 is max message count server can flush per second
		int max = 2000;
		int min = 100;
		int pending = this.manager.getPendingClientCount();
		pending = pending == 0 ? 1 : pending;
		int flushCount = 100000 / pending;
		if (flushCount > max)
			flushCount = max;
		if (flushCount < min)
			flushCount = min;
		
		while (!this.token.isCancelling()
				&& (this.pendingClient = this.manager.pollPendingClient()) != null) {
			this.pendingClient.flush(token, flushCount);
		}
	}
}
