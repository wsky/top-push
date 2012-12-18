package com.tmall.top.push;

import com.tmall.top.push.messaging.PublishMessage;

public class PublishSender extends Sender {

	public PublishSender(CancellationToken token, PushManager manager) {
		super(token, manager);
	}

	@Override
	protected void doSend() {
		PublishMessage msg;
		while ((msg = this.receiver.pollPublishMessage()) != null) {
			if (this.token.isCancelling)
				break;
			this.manager.getClient(msg.to).SendMessage(msg);
			this.receiver.release(msg);
		}
	}

}
