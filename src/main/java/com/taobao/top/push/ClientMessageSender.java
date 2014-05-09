package com.taobao.top.push;

import com.taobao.top.push.ClientConnection.SendStatus;

public class ClientMessageSender implements MessageSender {
	private ClientConnection[] connections;
	private int index;

	public void setConnections(ClientConnection[] connections) {
		this.connections = connections;
	}

	@Override
	public boolean send(Object message) {
		if (this.connections.length == 0)
			return false;

		// use copy avoid index out of range
		ClientConnection[] connections = this.connections;

		int begin = this.index++ % connections.length;
		int i = begin;

		do {
			ClientConnection connection = connections[i];

			if (connection == null)
				continue;

			if (!connection.isOpen())
				continue;

			SendStatus status;
			try {
				status = connection.sendMessage(message);
			} catch (Exception e) {
				// FIXME log error
				continue;
			}

			switch (status) {
			case SENT:
				return true;
			case DROP:
				return true;
			case RETRY:
				continue;
			}
		} while ((i = i + 1 >= connections.length ? 0 : i + 1) != begin);

		return false;
	}
}
