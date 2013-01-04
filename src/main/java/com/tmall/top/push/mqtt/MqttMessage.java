package com.tmall.top.push.mqtt;

import com.tmall.top.push.messages.Message;

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

	public MqttVariableHeader VariableHeader;

	public MqttMessage() {
		this.Header = new MqttHeader();
		this.VariableHeader = new MqttVariableHeader();
	}

	@Override
	protected void internalClear() {
		// TODO:clear header
		// TODO:clear more
	}
}