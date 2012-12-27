package com.tmall.top.push.messages;

public abstract class Message {
	public int messageType;
	// This flag is set when the client or server attempts to re-deliver a
	// PUBLISH/... message
	// public boolean duplicate;

	// if qos implement here, top-mq with retry features should be
	// included as broker storage
	// public Qos qos;
	// This flag is only used on PUBLISH messages. When a client sends a PUBLISH
	// to a server, if the Retain flag is set (1), the server should hold on to
	// the message after it has been delivered to the current subscribers
	// public boolean retain;

	public int remainingLength;
	// full length include head(messageType)/to
	public int fullMessageSize;

	// message from client id
	// need be filled when receiving
	public String from;
	// target client id
	public String to;

	public Object body;

	public void clear() {
		this.messageType = 0;
		this.remainingLength = 0;
		this.fullMessageSize = 0;
		this.from = null;
		this.to = null;
		this.body = null;
		this.internalClear();
	}
	
	public String getTo() {
		return null;
	}
	
	public String getFrom() {
		return null;
	}

	protected abstract void internalClear();
}
