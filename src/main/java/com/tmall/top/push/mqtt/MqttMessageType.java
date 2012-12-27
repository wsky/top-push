package com.tmall.top.push.mqtt;

// An enumeration of all available MQTT Message Types
public final class MqttMessageType {
	// Reserved by the MQTT spec, should not be used.
	public final static int Reserved1 = 0;

	public final static int Connect = 1;
	public final static int ConnectAck = 2;
	public final static int Publish = 3;
	public final static int PublishAck = 4;
	public final static int PublishReceived = 5;
	public final static int PublishRelease = 6;
	public final static int PublishComplete = 7;
	public final static int Subscribe = 8;
	public final static int SubscribeAck = 9;
	public final static int Unsubscribe = 10;
	public final static int UnsubscribeAck = 11;
	public final static int PingRequest = 12;
	public final static int PingResponse = 13;
	public final static int Disconnect = 14;

	// Reserved by the MQTT spec, should not be used.
	public final static int Reserved2 = 15;
}
