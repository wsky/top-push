package com.tmall.top.push;

public class Sender implements Runnable {
	protected CancellationToken token;
	protected PushManager manager;
	private Client pendingClient;
	private int idle;

	public CancellationToken getCancellationToken() {
		return this.token;
	}

	public Sender(PushManager manager, CancellationToken token, int idle) {
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
		while (!this.token.isCancelling()
				&& (this.pendingClient = this.manager.pollPendingClient()) != null) {
			this.pendingClient.flush(token, 1000);
		}
	}
}
