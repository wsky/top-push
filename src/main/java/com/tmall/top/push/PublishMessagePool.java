package com.tmall.top.push;

import com.tmall.top.push.messaging.PublishMessage;

public class PublishMessagePool extends Pool<PublishMessage> {

	public PublishMessagePool(int poolSize) {
		super(poolSize);
	}

	@Override
	public PublishMessage createNew() {
		return new PublishMessage();
	}

}
