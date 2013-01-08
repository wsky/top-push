package com.tmall.top.push.messages;

public class MessageType {
	// following semantics was only known by client
	
	// publish message
	// publisher->server
	// server->subscriber
	public final static int PUBLISH = 1;
	// PUBACK,
	// batch pub and batch confirm
	public final static int PUBCONFIRM = 2;
}