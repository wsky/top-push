package top.push;

import static org.junit.Assert.*;

import org.junit.Test;

import top.push.Client;

public class ClientTest {
	@Test
	public void add_remove_connection_test() {
		Client client = new Client(1);
		ConnectionMock c1 = new ConnectionMock();
		ConnectionMock c2 = new ConnectionMock();

		client.addConnection(c1);
		client.addConnection(c2);
		assertEquals(client.getId(), c1.getClientId());
		assertEquals(client.getId(), c2.getClientId());
		assertEquals(2, client.getConnections().length);
		assertEquals(2, client.getConnectionCount());

		client.removeConnection(c1);
		assertEquals(1, client.getConnections().length);
		assertEquals(1, client.getConnectionCount());
	}

	@Test
	public void connection_count_test() {
		Client client = new Client(1);
		ConnectionMock c1 = new ConnectionMock(false, false);
		ConnectionMock c2 = new ConnectionMock(true, false);
		client.addConnection(c1);
		client.addConnection(c2);
		assertEquals(2, client.getConnectionCount());
		assertEquals(1, client.getValidConnectionCount());
	}
}
