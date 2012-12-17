package com.tmall.top.push;

import com.tmall.top.push.messaging.ConfirmMessage;

public class ConfirmMessagePool extends Pool<ConfirmMessage>{

	public ConfirmMessagePool(int poolSize) {
		super(poolSize);
	}

	@Override
	public ConfirmMessage createNew() {
		return new ConfirmMessage();
	}

}
