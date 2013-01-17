package com.tmall.top.push;

import com.tmall.top.push.messages.Message;
import com.tmall.top.push.mqtt.connect.MqttConnectMessage;
import com.tmall.top.push.mqtt.disconnect.MqttDisconnectMessage;

// deal with command message like CONNECT/DISCONNECT
public class Processor {
	public void process(Message message, ClientConnection connection)
			throws Exception {
		if (!"mqtt".equalsIgnoreCase(connection.getProtocol())) {
			System.out.println("Only mqtt protocol support command message process, currently");
			return;
		}

		System.err.println("ignore MQTT Message|" + message);

		// TODO: Actually implement MQTT CONNECT/DISCONNECT
		if (message instanceof MqttConnectMessage) {

		} else if (message instanceof MqttDisconnectMessage) {

		}
	}
}