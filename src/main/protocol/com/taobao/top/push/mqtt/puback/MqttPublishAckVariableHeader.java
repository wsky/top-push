package com.taobao.top.push.mqtt.puback;

import com.taobao.top.push.mqtt.MqttVariableHeader;

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
