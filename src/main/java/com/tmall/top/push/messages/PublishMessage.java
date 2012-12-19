package com.tmall.top.push.messages;

public class PublishMessage extends Message {
	public String id;

	@Override
	protected void internalClear() {
		this.id = null;
	}
}
