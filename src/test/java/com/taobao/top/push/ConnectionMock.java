package com.taobao.top.push;

public class ConnectionMock extends ClientConnection {
	public int sendCount;
	private boolean isOpen;
	private boolean canSend;

	public ConnectionMock() {
		this(true, true);
	}

	public ConnectionMock(boolean isOpen, boolean canSend) {
		super(null, null);
		this.isOpen = isOpen;
		this.canSend = canSend;
	}

	@Override
	public boolean isOpen() {
		return this.isOpen;
	}

	@Override
	public SendStatus sendMessage(Object msg) throws Exception {
		if (!this.canSend)
			throw new Exception("send message exception mock!");
		this.sendCount++;
		return SendStatus.SENT;
	}

	@Override
	public void close(String reasonText) {
	}

}