package com.taobao.top.push;

import static org.junit.Assert.*;

import org.junit.Test;

import com.taobao.top.push.Client;
import com.taobao.top.push.PushManager;

public class PushMangerTest {
	@Test
	public void get_client_test() throws Exception {
		PushManager manager = new PushManager();
		Object id = "abc";
		assertNull(manager.getClient(id));
		manager.connectClient(id, new ConnectionMock());
		assertNotNull(manager.getClient(id));
	}

	@Test
	public void state_test() throws Exception {
		PushManager manager = new PushManager();
		manager.setStateBuilderPeriod(0);
		manager.setClientStateHandler(new ClientStateHandler() {
			@Override
			public void onClientPending(Client client) {
				System.out.println("pending: " + client.getId());
			}

			@Override
			public void onClientOffline(Client client) {
				System.out.println("offline: " + client.getId());
			}

			@Override
			public void onClientIdle(Client client) {
				System.out.println("idle: " + client.getId());
			}

			@Override
			public void onClientDisconnect(Client client, ClientConnection clientConnection, String reasonText) {
				System.out.println("disconnect: " + client.getId());
			}

			@Override
			public void onClientConnect(Client client, ClientConnection clientConnection) {
				System.out.println("connect: " + client.getId());
			}
		});

		Client c1 = manager.connectClient("1", new ConnectionMock(false, false));
		Client c2 = manager.connectClient("2", new ConnectionMock(false, false));
		manager.rebuildState();
		assertEquals(0, manager.getConnectionCount());
		assertEquals(0, manager.getClient(c1.getId()).getConnectionsCount());
		assertEquals(0, manager.getClient(c2.getId()).getConnectionsCount());

		c1.addConnection(new ConnectionMock());
		manager.rebuildState();
		assertEquals(1, manager.getClient(c1.getId()).getConnectionsCount());
		assertEquals(0, manager.getClient(c2.getId()).getConnectionsCount());
	}

	@Test
	public void clients_perf_test() {
		PushManager manager = new PushManager();
		int total = 100000;
		for (int i = 0; i < total; i++)
			manager.getOrCreateClient(this.parseId(i));

		long begin = System.currentTimeMillis();
		manager.rebuildState();
		System.out.println(System.currentTimeMillis() - begin);

		begin = System.currentTimeMillis();
		for (int i = 0; i < total; i++)
			manager.getClient(this.parseId(i));
		System.out.println(System.currentTimeMillis() - begin);
	}

	private Object parseId(int i) {
		return i;// Integer.toString(i)
	}
}