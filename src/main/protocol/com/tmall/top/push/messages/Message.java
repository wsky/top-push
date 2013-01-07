package com.tmall.top.push.messages;

public class Message {
	public int messageType;

	// message from client id
	// need be filled when receiving
	public String from;
	// target client id
	public String to;

	public int bodyFormat;

	public int remainingLength;

	public Object body;

	public int fullMessageSize;

	public void clear() {
		this.messageType = 0;
		this.from = null;
		this.to = null;
		this.bodyFormat = 0;
		this.remainingLength = 0;
		this.body = null;

		this.fullMessageSize = 0;
	}
}
