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

	public boolean send(Object message) {
		// use copy
		Queue<ClientConnection> connections = this.connections;

		do {
			ClientConnection connection = connections.poll();

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
				connections.add(connection);
				return true;
			case DROP:
				return true;
			case RETRY:
				continue;
			}
		} while (true);
	}
}
