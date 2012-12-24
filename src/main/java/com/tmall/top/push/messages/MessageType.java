package com.tmall.top.push.messages;

public class MessageType {
	// http://public.dhe.ibm.com/software/dw/webservices/ws-mqtt/mqtt-v3r1.html#msg-format
	// like MQTT
	// CONNECT,
	// CONNACK,
	// DISCONNECT,
	// SUBSCRIBE,
	// SUBACK,
	// UNSUBSCRIBE,
	// UNSUBACK,
	// PINGREQ,
	// PINGRESP,

	// publish message
	// publisher->server
	// server->subscriber
	public final static int PUBLISH = 1;
	// one pub, one acknowledge
	// public final static int PUBACK = 2;
	// PUBACK,
	// batch pub and batch confirm
	public final static int PUBCONFIRM = 2;
}