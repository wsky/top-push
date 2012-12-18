package com.tmall.top.push.messaging;

public abstract class Message {
	public int messageType;
	// This flag is set when the client or server attempts to re-deliver a
	// PUBLISH/... message
	// public boolean duplicate;

	// TODO: if qos implement here, top-mq with retry features should be
	// included as broker storage
	// public Qos qos;
	// This flag is only used on PUBLISH messages. When a client sends a PUBLISH
	// to a server, if the Retain flag is set (1), the server should hold on to
	// the message after it has been delivered to the current subscribers
	// public boolean retain;

	// message body payload
	public int messageSize;

	public Object body;

	public void clear() {
		this.messageType = 0;
		this.messageSize = 0;
		this.body = null;
		this.internalClear();
	}
	
	protected abstract void internalClear();
}
