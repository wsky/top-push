package com.taobao.top.push;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.taobao.top.push.Client;
import com.taobao.top.push.PushManager;

public class PushMangerTest {
	@Test
	public void get_client_test() throws Exception {
		PushManager manager = new PushManager(new DefaultLoggerFactory(), 10, 0, 100);
		Object id = new DefaultIdentity("abc");
		assertNull(manager.getClient(id));
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("id", "abc");
		manager.connectClient(headers, new ConnectionWrapper());
		assertNotNull(manager.getClient(id));
	}

	@Test
	public void state_test() throws Exception {
		// senderCount should be 0
		PushManager manager = new PushManager(new DefaultLoggerFactory(), 2, 0, 100) {
			@Override
			protected void prepareChecker(int stateBuilderIdle) {
			}

			@Override
			protected void rebuildClientsState(Client client, boolean noPending, boolean pending, boolean offline) {
				System.out.println(String.format("id=%s, noPending=%s, pending=%s, offline=%s", client.getId(), noPending, pending, offline));
				super.rebuildClientsState(client, noPending, pending, offline);
			}
		};

		Map<String, String> headers = new HashMap<String, String>();
		headers.put("id", "1");
		Client c1 = manager.connectClient(headers, new ConnectionWrapper(false, false));
		c1.pendingMessage(new Object());

		headers.put("id", "2");
		Client c2 = manager.connectClient(headers, new ConnectionWrapper(false, false));
		c2.pendingMessage(new Object());

		manager.rebuildClientsState();
		// offline
		assertTrue(manager.isOfflineClient(c1.getId()));
		assertTrue(manager.isOfflineClient(c2.getId()));
		assertEquals(0, manager.getPendingClientCount());

		c1.AddConnection(new ConnectionWrapper());
		manager.rebuildClientsState();
		// pending
		assertEquals(1, manager.getPendingClientCount());
		assertEquals(c1, manager.pollPendingClient());
		assertTrue(manager.isOfflineClient(c2.getId()));

		c1.flush(new CancellationToken(), 10);
		manager.rebuildClientsState();
		// idle
		assertTrue(manager.isIdleClient(c1.getId()));
		assertEquals(0, manager.getPendingClientCount());

		c1.AddConnection(new ConnectionWrapper());
		manager.rebuildClientsState();
		assertTrue(manager.isReachMaxConnectionCount());
	}
}