package com.taobao.top.push;

public class ConnectionWrapper extends ClientConnection {
	public int sendCount;
	private boolean isOpen;
	private boolean canSend;

	public ConnectionWrapper() {
		this(true, true);
	}

	public ConnectionWrapper(boolean isOpen, boolean canSend) {
		this.isOpen = isOpen;
		this.canSend = canSend;
	}

	@Override
	protected void initHeaders() {
	}

	@Override
	protected void internalClear() {
	}

	@Override
	public boolean isOpen() {
		return this.isOpen;
	}

	@Override
	public void sendMessage(Object msg) throws Exception {
		if (!this.canSend)
			throw new Exception("send message exception mock!");
		this.sendCount++;
		ClientTest.sendCount++;
	}

}