package com.taobao.top.push.mqtt;

// Represents the base class for the Variable Header portion of some MQTT Messages.
public class MqttVariableHeader {
	public int Length;
	
	public String ProtocolName;
	
	public byte ProtocolVersion;
	
	public MqttConnectFlags ConnectFlags;

	public short KeepAlive;
	
	public int ReturnCode;
	
	public String TopicName;
	
	public short MessageIdentifier;

	public MqttVariableHeader() {
		this.ProtocolName = "MQIsdp";
		this.ProtocolVersion = 3;
		this.ConnectFlags = new MqttConnectFlags();
	}

	protected int getReadFlags() {
		return 0;
	}

	protected int getWriteFlags() {
		return 0;
	}

	public class ReadWriteFlags {
		public final static int ProtocolName = 1;
		public final static int ProtocolVersion = 2;
		public final static int ConnectFlags = 4;
		public final static int KeepAlive = 8;
		public final static int ReturnCode = 16;
		public final static int TopicName = 32;
		public final static int MessageIdentifier = 64;
	}
}
