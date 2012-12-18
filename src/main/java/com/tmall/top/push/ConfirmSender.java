package com.tmall.top.push;

import com.tmall.top.push.messaging.PublishConfirmMessage;

public class ConfirmSender extends Sender {
	
	public ConfirmSender(CancellationToken token, PushManager manager) {
		super(token, manager);
	}

	@Override
	protected void doSend() {
		PublishConfirmMessage msg;
		// TODO:send batch confirm 1024/8=128
		while ((msg = this.receiver.pollConfirmMessage()) != null) {
			if (this.token.isCancelling)
				break;
			this.manager.getConfirmClient().SendMessage(msg);
			this.receiver.release(msg);
		}
	}

}