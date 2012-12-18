package com.tmall.top.push.messaging;

public class PublishMessage extends Message {
	// target client id
	public String to;
	public String id;

	@Override
	protected void internalClear() {
		this.to = null;
		this.id = null;
	}
}
