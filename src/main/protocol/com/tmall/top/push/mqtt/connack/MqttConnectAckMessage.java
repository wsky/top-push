package com.tmall.top.push.mqtt.connack;

import com.tmall.top.push.mqtt.MqttHeader;
import com.tmall.top.push.mqtt.MqttMessage;
import com.tmall.top.push.mqtt.MqttMessageType;

public class MqttConnectAckMessage extends MqttMessage {
	public MqttConnectAckVariableHeader VariableHeader;

	public MqttConnectAckMessage() {
		this.Header = new MqttHeader();
		this.Header.MessageType = MqttMessageType.ConnectAck;
		this.VariableHeader = new MqttConnectAckVariableHeader();
	}
}
