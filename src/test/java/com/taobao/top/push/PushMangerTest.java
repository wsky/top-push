package com.taobao.top.push;

import static org.junit.Assert.*;

import org.apache.commons.lang.time.StopWatch;
import org.junit.Test;

import com.taobao.top.push.Client;
import com.taobao.top.push.PushManager;
import com.taobao.top.push.messages.Message;
import com.taobao.top.push.websocket.WebSocketClientConnection;

public class PushMangerTest {
	@Test
	public void get_client_test() {
		PushManager manager = new PushManager(new DefaultLoggerFactory(), 10, 1, 1000, 100);
		Identity id = new DefaultIdentity("abc");
		assertEquals(manager.getClient(id), manager.getClient(id));

		StopWatch watch = new StopWatch();
		watch.start();
		int count = 100000;
		for (int i = 0; i < count; i++)
			manager.getClient(new DefaultIdentity("abc" + i));
		watch.stop();
		System.out.println(String.format("%s clients cost %sms", count,
				watch.getTime()));
		// 100000 clients cost 325ms
		// poor performance

		StopWatch watch2 = new StopWatch();
		watch2.start();
		for (int i = 0; i < count; i++)
			manager.getClient(id);
		watch2.stop();
		System.out.println(String.format("get client %s cost %sms", count,
				watch2.getTime()));
	}

	@Test
	public void state_test() throws SecurityException, NoSuchMethodException,
			InterruptedException {
		// senderCount should be 0
		PushManager manager = new PushManager(new DefaultLoggerFactory(), 2, 0, 1000, 100);
		Client c1 = manager.getClient(new DefaultIdentity("1"));
		Client c2 = manager.getClient(new DefaultIdentity("2"));
		Thread.sleep(1000);
		assertTrue(manager.isOfflineClient(c1.getId()));
		assertTrue(manager.isOfflineClient(c2.getId()));
		assertNull(manager.pollPendingClient());

		c1.AddConnection(new WebSocketClientConnection());
		Thread.sleep(1000);
		assertTrue(manager.isIdleClient(c1.getId()));
		assertTrue(manager.isOfflineClient(c2.getId()));
		assertNull(manager.pollPendingClient());
		assertFalse(manager.isReachMaxConnectionCount());

		c1.pendingMessage(new Message());
		Thread.sleep(1000);
		assertFalse(manager.isIdleClient(c1.getId()));
		assertNotNull(manager.pollPendingClient());

		c1.AddConnection(new WebSocketClientConnection());
		Thread.sleep(1000);
		assertTrue(manager.isReachMaxConnectionCount());
	}
}