package com.taobao.top.push.mqtt.publish;

import com.taobao.top.push.mqtt.MqttHeader;
import com.taobao.top.push.mqtt.MqttMessage;
import com.taobao.top.push.mqtt.MqttMessageType;

public class MqttPublishMessage extends MqttMessage {
	// http://public.dhe.ibm.com/software/dw/webservices/ws-mqtt/mqtt-v3r1.html#publish
	// A PUBLISH message is sent by a client to a server for distribution to
	// interested subscribers. Each PUBLISH message is associated with a topic
	// name (also known as the Subject or Channel). This is a hierarchical name
	// space that defines a taxonomy of information sources for which
	// subscribers can register an interest. A message that is published to a
	// specific topic name is delivered to connected subscribers for that topic.

	// If a client subscribes to one or more topics, any message published to
	// those topics are sent by the server to the client as a PUBLISH message.

	public MqttPublishVariableHeader VariableHeader;

	public MqttPublishMessage() {
		this.Header = new MqttHeader();
		this.Header.MessageType = MqttMessageType.Publish;
		this.VariableHeader = new MqttPublishVariableHeader(this.Header);
	}

	@Override
	public void clear() {
		super.clear();
		this.VariableHeader.MessageIdentifier = 0;
		this.VariableHeader.TopicName = null;
	}
}
