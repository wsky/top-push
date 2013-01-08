package com.tmall.top.push.mqtt.disconnect;

import com.tmall.top.push.mqtt.MqttHeader;
import com.tmall.top.push.mqtt.MqttMessage;
import com.tmall.top.push.mqtt.MqttMessageType;

public class MqttDisconnectMessage extends MqttMessage {

	public MqttDisconnectMessage() {
		this.Header = new MqttHeader();
		this.Header.MessageType = MqttMessageType.Disconnect;
	}
}
