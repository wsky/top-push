package com.tmall.top.push.messages;

import com.tmall.top.push.Pool;

@Deprecated
public class PublishMessagePool extends Pool<PublishMessage> {

	public PublishMessagePool(int poolSize) {
		super(poolSize);
	}

	@Override
	public PublishMessage createNew() {
		return new PublishMessage();
	}

}
