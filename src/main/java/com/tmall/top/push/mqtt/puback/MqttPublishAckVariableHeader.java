package com.tmall.top.push.mqtt.puback;

import com.tmall.top.push.mqtt.MqttVariableHeader;

public class MqttPublishAckVariableHeader extends MqttVariableHeader {

	@Override
	protected int getReadFlags() {
		return ReadWriteFlags.MessageIdentifier;
	}

	@Override
	protected int getWriteFlags() {
		return ReadWriteFlags.MessageIdentifier;
	}
}
