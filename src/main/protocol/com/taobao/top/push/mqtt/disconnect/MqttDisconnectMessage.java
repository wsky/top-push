package com.taobao.top.push.mqtt.disconnect;

import com.taobao.top.push.mqtt.MqttHeader;
import com.taobao.top.push.mqtt.MqttMessage;
import com.taobao.top.push.mqtt.MqttMessageType;

public class MqttDisconnectMessage extends MqttMessage {

	public MqttDisconnectMessage() {
		this.Header = new MqttHeader();
		this.Header.MessageType = MqttMessageType.Disconnect;
	}
}
