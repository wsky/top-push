package com.taobao.top.push.mqtt.connect;

import com.taobao.top.push.mqtt.MqttVariableHeader;

public class MqttConnectVariableHeader extends MqttVariableHeader {

	@Override
	protected int getReadFlags() {
		return ReadWriteFlags.ProtocolName | ReadWriteFlags.ProtocolVersion
				| ReadWriteFlags.ConnectFlags | ReadWriteFlags.KeepAlive;
	}

	@Override
	protected int getWriteFlags() {
		return this.getReadFlags();
	}
}
