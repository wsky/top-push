package com.tmall.top.push;

import java.util.concurrent.ConcurrentLinkedQueue;

import com.tmall.top.push.messaging.Message;

public class Client {
	private String id;
	private ConcurrentLinkedQueue<ClientConnection> connections;

	public String getId() {
		return this.id;
	}

	public Client(String id) {
		this.id = id;
		this.connections = new ConcurrentLinkedQueue<ClientConnection>();
	}

	public void AddConnection(ClientConnection conn) {
		this.connections.add(conn);
		System.out.println(String.format("client#%s add new connection from %s",
				this.getId(), conn.getOrigin()));
	}

	public void RemoveConnection(ClientConnection conn) {
		this.connections.remove(conn);
		System.out.println(String.format("client#%s remove a connection from %s",
				this.getId(), conn.getOrigin()));
	}

	public void SendMessage(Message msg) {
		while (true) {
			ClientConnection connection = this.connections.poll();
			if (connection == null)
				break;
			if (!connection.isOpen()) {
				System.out.println(String.format(
						"connection#%s[%s] is closed, remove it",
						connection.getId(), connection.getOrigin()));
				continue;
			}
			try {
				connection.sendMessage(msg);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				this.AddConnection(connection);
			}
		}

	}
}
