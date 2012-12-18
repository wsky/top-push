package com.tmall.top.push;

public abstract class Sender implements Runnable {
	protected CancellationToken token;
	protected PushManager manager;
	protected Receiver receiver;

	public CancellationToken getCancellationToken() {
		return this.token;
	}

	public Sender(CancellationToken token, PushManager manager) {
		this.token = token;
		this.manager = manager;
		this.receiver = this.manager.getReceiver();
	}

	@Override
	public void run() {
		while (!this.token.isCancelling) {
			try {
				doSend();
			} catch (Exception e) {
				e.printStackTrace();
			}

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	protected abstract void doSend();
}
