package com.taobao.top.push;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.taobao.top.push.ClientConnection.SendStatus;

public class BatchedMessageSender implements MessageSender {
	private static Random RANDOM = new Random();

	private Queue<ClientConnection> connections;

	public BatchedMessageSender() {
	}

	protected BatchedMessageSender(Queue<ClientConnection> connections) {
		this.connections = connections;
	}

	@Override
	public void setConnections(ClientConnection[] connections) {
		List<ClientConnection> list = Arrays.asList(connections);
		Collections.shuffle(list, RANDOM);
		this.connections = new ConcurrentLinkedQueue<ClientConnection>(list);
	}

	public MessagingStatus send(Object message) {
		if (this.connections.isEmpty())
			return MessagingStatus.NONE_CONNECTION;

		// use copy
		Queue<ClientConnection> connections = this.connections;

		do {
			ClientConnection connection = connections.poll();

			if (connection == null)
				break;

			if (!connection.isValid())
				continue;

			SendStatus status;
			try {
				status = connection.sendMessage(message);
			} catch (Exception e) {
				continue;
			}

			switch (status) {
			case SENT:
				connections.add(connection);
				return MessagingStatus.SENT;
			case IN_DOUBT:
				return MessagingStatus.IN_DOUBT;
			case RETRY:
				continue;
			}
		} while (true);

		return MessagingStatus.FAULT;
	}
}
