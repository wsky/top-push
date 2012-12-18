package com.tmall.top.push;

import com.tmall.top.push.messaging.PublishConfirmMessage;

public class PublishConfirmMessagePool extends Pool<PublishConfirmMessage>{

	public PublishConfirmMessagePool(int poolSize) {
		super(poolSize);
	}

	@Override
	public PublishConfirmMessage createNew() {
		return new PublishConfirmMessage();
	}

}
