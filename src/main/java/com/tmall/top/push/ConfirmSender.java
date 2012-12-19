package com.tmall.top.push;

@Deprecated
public class ConfirmSender extends Sender {
	private final static String CLIENT_CONFIRM = "confirm";

	public ConfirmSender(PushManager manager, CancellationToken token, int idle) {
		super(manager, token, idle);
	}

	@Override
	protected void doSend() {
		this.manager.getClient(CLIENT_CONFIRM).flush(this.token, 50000);
	}

}