package com.tmall.top.push.messages;

import com.tmall.top.push.Pool;

@Deprecated
public class PublishConfirmMessagePool extends Pool<PublishConfirmMessage>{

	public PublishConfirmMessagePool(int poolSize) {
		super(poolSize);
	}

	@Override
	public PublishConfirmMessage createNew() {
		return new PublishConfirmMessage();
	}

}
