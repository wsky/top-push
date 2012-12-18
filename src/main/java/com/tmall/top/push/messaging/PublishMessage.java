package com.tmall.top.push.messaging;

public class PublishMessage extends Message {
	public String id;

	@Override
	protected void internalClear() {
		this.id = null;
	}
}
