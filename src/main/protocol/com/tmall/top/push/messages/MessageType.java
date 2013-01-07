package com.tmall.top.push.messages;

// it was known by client
public class MessageType {
	// publish message
	// publisher->server
	// server->subscriber
	public final static int PUBLISH = 1;
	// PUBACK,
	// batch pub and batch confirm
	public final static int PUBCONFIRM = 2;
}