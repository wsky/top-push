package com.tmall.top.push.mqtt;

// Represents the connect flags part of the MQTT Variable Header
public class MqttConnectFlags {
	public boolean Reserved1;
	public boolean CleanStart;
	public boolean WillFlag;
	public int WillQos;
	public boolean WillRetain;
	public boolean Reserved2;
	public boolean Reserved3;
}
