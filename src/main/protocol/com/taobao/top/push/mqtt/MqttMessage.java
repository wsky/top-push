package com.taobao.top.push.mqtt;

import com.taobao.top.push.messages.Message;

// Represents an MQTT message that contains a fixed header, variable header and message body.
public class MqttMessage extends Message {
	// / ----------------------------
	// / | Header, 2-5 Bytes Length |
	// / ----------------------------
	// / | Variable Header |
	// / | n Bytes Length |
	// / ----------------------------
	// / | Message Payload |
	// / | 256MB minus VH Size |

	public MqttHeader Header;

	public MqttMessage() {
		this.Header = new MqttHeader();
	}

	@Override
	public void clear() {
		super.clear();
		this.Header.Duplicate = false;
		this.Header.Length = 0;
		this.Header.Qos = MqttQos.AtMostOnce;
		this.Header.Retain = false;
	}
}