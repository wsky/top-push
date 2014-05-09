package com.taobao.top.push;

import java.util.Queue;

import com.taobao.top.push.ClientConnection.SendStatus;

public class BatchedMessageSender implements MessageSender {
	private Queue<ClientConnection> connections;

	public BatchedMessageSender(Queue<ClientConnection> connections) {
		this.connections = connections;
	}

	public boolean send(Object message) {
		do {
			ClientConnection connection = this.connections.poll();

			if (connection == null)
				return false;

			if (!connection.isOpen())
				continue;

			SendStatus status;
			try {
				status = connection.sendMessage(message);
			} catch (Exception e) {
				continue;
			}

			switch (status) {
			case SENT:
				this.connections.add(connection);
				return true;
			case DROP:
				return true;
			case RETRY:
				continue;
			}
		} while (true);
	}
}
