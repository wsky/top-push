package com.taobao.top.push;

import com.taobao.top.push.messages.Message;
import com.taobao.top.push.mqtt.connect.MqttConnectMessage;
import com.taobao.top.push.mqtt.disconnect.MqttDisconnectMessage;

// deal with command message like CONNECT/DISCONNECT
public class Processor {
	private Logger logger;

	public Processor(LoggerFactory loggerFactory) {
		this.logger = loggerFactory.create(this);
	}

	public void process(Message message, ClientConnection connection)
			throws Exception {
		if (!"mqtt".equalsIgnoreCase(connection.getProtocol())) {
			this.logger.warn("Only mqtt protocol support command message process, currently");
			return;
		}

		this.logger.warn("ignore MQTT Message|" + message);

		// TODO: Actually implement MQTT CONNECT/DISCONNECT
		if (message instanceof MqttConnectMessage) {

		} else if (message instanceof MqttDisconnectMessage) {

		}
	}
}