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
		PushManager manager = new PushManager(new DefaultLoggerFactory(), 10, 1, 100);
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
		PushManager manager = new PushManager(new DefaultLoggerFactory(), 2, 0, 100);

		Map<String, String> headers = new HashMap<String, String>();
		headers.put("id", "1");
		Client c1 = manager.connectClient(headers, new ConnectionWrapper(false, false));
		c1.pendingMessage(new Object());
		c1.flush(new CancellationToken(), 1);
		headers.put("id", "2");
		Client c2 = manager.connectClient(headers, new ConnectionWrapper(false, false));
		c2.pendingMessage(new Object());
		c2.flush(new CancellationToken(), 1);

		Thread.sleep(1000);
		assertTrue(manager.isOfflineClient(c1.getId()));
		assertTrue(manager.isOfflineClient(c2.getId()));
		assertNull(manager.pollPendingClient());

		c1.AddConnection(new ConnectionWrapper());
		Thread.sleep(1000);
		assertTrue(manager.isIdleClient(c1.getId()));
		assertTrue(manager.isOfflineClient(c2.getId()));
		assertNull(manager.pollPendingClient());
		assertFalse(manager.isReachMaxConnectionCount());

		c1.pendingMessage(new Object());
		Thread.sleep(1000);
		assertFalse(manager.isIdleClient(c1.getId()));
		assertNotNull(manager.pollPendingClient());

		c1.AddConnection(new ConnectionWrapper());
		Thread.sleep(1000);
		assertTrue(manager.isReachMaxConnectionCount());
	}
}