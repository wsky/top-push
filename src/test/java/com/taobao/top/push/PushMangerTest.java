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
		PushManager manager = new PushManager(new DefaultLoggerFactory(), 0, 100);
		Object id = new DefaultIdentity("abc");
		assertNull(manager.getClient(id));
		manager.connectClient(id, new ConnectionWrapper());
		assertNotNull(manager.getClient(id));
	}

	@Test
	public void state_test() throws Exception {
		// senderCount should be 0
		PushManager manager = new PushManager(new DefaultLoggerFactory(), 0, 100) {
			@Override
			protected void prepareChecker(int stateBuilderIdle) {
			}
		};
		manager.setMaxConnectionCount(2);

		Map<String, String> headers = new HashMap<String, String>();
		headers.put("id", "1");
		Client c1 = manager.connectClient("1", new ConnectionWrapper(false, false));
		c1.pendingMessage(new Object());

		headers.put("id", "2");
		Client c2 = manager.connectClient("2", new ConnectionWrapper(false, false));
		c2.pendingMessage(new Object());

		manager.rebuildClientsState();
		// offline
		assertEquals(ClientStatus.Offline, manager.getClient(c1.getId()).getStatus());
		assertEquals(ClientStatus.Offline, manager.getClient(c2.getId()).getStatus());

		c1.AddConnection(new ConnectionWrapper());
		manager.rebuildClientsState();
		// pending
		assertEquals(ClientStatus.Pending, manager.getClient(c1.getId()).getStatus());
		assertEquals(ClientStatus.Offline, manager.getClient(c2.getId()).getStatus());

		c1.flush(new CancellationToken(), 10);
		manager.rebuildClientsState();
		// idle
		assertEquals(ClientStatus.Idle, manager.getClient(c1.getId()).getStatus());

		c1.AddConnection(new ConnectionWrapper());
		manager.rebuildClientsState();
		assertTrue(manager.isReachMaxConnectionCount());
	}
}