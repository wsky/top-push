package com.taobao.top.push;

public class CancellationToken {
	// something pending
	//private boolean isPending;
	// server stoping
	private boolean isStoping;
	// cancel current work
	private boolean isCancelling;

	public boolean isStoping() {
		return this.isStoping;
	}

	public boolean isCancelling() {
		return this.isStoping || this.isCancelling;
	}

	public void stop() {
		this.isStoping = true;
	}

	public void setCancelling(boolean isCancelling) {
		this.isCancelling = isCancelling;
	}
}
