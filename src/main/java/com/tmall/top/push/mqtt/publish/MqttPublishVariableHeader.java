package com.tmall.top.push.mqtt.publish;

import com.tmall.top.push.mqtt.MqttHeader;
import com.tmall.top.push.mqtt.MqttQos;
import com.tmall.top.push.mqtt.MqttVariableHeader;

public class MqttPublishVariableHeader extends MqttVariableHeader {

	private MqttHeader header;

	public MqttPublishVariableHeader(MqttHeader header) {
		this.header = header;
	}

	@Override
	protected int getReadFlags() {
		if (this.header.Qos == MqttQos.AtLeastOnce
				|| this.header.Qos == MqttQos.ExactlyOnce) {
			return ReadWriteFlags.TopicName | ReadWriteFlags.MessageIdentifier;
		} else {
			return ReadWriteFlags.TopicName;
		}
	}

	@Override
	protected int getWriteFlags() {
		return this.getReadFlags();
	}
}
