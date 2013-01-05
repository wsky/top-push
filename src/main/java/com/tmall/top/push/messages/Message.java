package com.tmall.top.push.messages;

public class Message {
	public int messageType;
	
	public int remainingLength;
	
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
	}
}
