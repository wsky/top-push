package com.taobao.top.push.mqtt;

// Represents the Fixed Header of an MQTT message.
public class MqttHeader {
	// The type of the MQTT message.
	public int MessageType;

	// indicating whether this MQTT Message is duplicate of a previous message.
	public boolean Duplicate;

	// Gets or sets the Quality of Service indicator for the message.
	public int Qos;

	// indicating whether this MQTT message should be retained by the message
	// broker for transmission to new subscribers.
	public boolean Retain;

	// Backing storage for the payload size.
	public int RemainingLength;
	
	// full header cost bytes
	public int Length;
}
