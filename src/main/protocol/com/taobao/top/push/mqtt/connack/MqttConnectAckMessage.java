package com.taobao.top.push.mqtt.connack;

import com.taobao.top.push.mqtt.MqttHeader;
import com.taobao.top.push.mqtt.MqttMessage;
import com.taobao.top.push.mqtt.MqttMessageType;

public class MqttConnectAckMessage extends MqttMessage {
	public MqttConnectAckVariableHeader VariableHeader;

	public MqttConnectAckMessage() {
		this.Header = new MqttHeader();
		this.Header.MessageType = MqttMessageType.ConnectAck;
		this.VariableHeader = new MqttConnectAckVariableHeader();
	}
}
