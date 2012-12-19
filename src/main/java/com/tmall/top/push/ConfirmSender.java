package com.tmall.top.push;

@Deprecated
public class ConfirmSender extends Sender {
	private final static String CLIENT_CONFIRM = "confirm";

	public ConfirmSender(PushManager manager, CancellationToken token, int idle) {
		super(manager, token, idle);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void doSend() {
		// TODO:just support one type confirm backend
		this.manager.getClient(CLIENT_CONFIRM).flush(this.token, 50000);
	}

}