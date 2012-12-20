package com.tmall.top;

import static org.junit.Assert.*;

import org.apache.commons.lang.time.StopWatch;
import org.junit.Test;

import com.tmall.top.push.Client;
import com.tmall.top.push.PushManager;
import com.tmall.top.push.messages.PublishMessage;
import com.tmall.top.push.websocket.WebSocketClientConnection;

public class PushMangerTest {
	@Test
	public void get_client_test() {
		PushManager manager = new PushManager(1024, 1024, 10, 10, 1, 1000, 1000);
		String id = "abc";
		assertEquals(manager.getClient(id), manager.getClient(id));

		StopWatch watch = new StopWatch();
		watch.start();
		int count = 100000;
		for (int i = 0; i < count; i++)
			manager.getClient(id + i);
		watch.stop();
		System.out.println(String.format("%s clients cost %sms", count,
				watch.getTime()));
		// 100000 clients cost 325ms
		// poor performance
	}

	@Test
	public void state_test() throws SecurityException, NoSuchMethodException,
			InterruptedException {
		PushManager manager = new PushManager(1024, 1024, 10, 10, 1, 1000, 100);
		Client c1 = manager.getClient("1");
		Client c2 = manager.getClient("2");
		Thread.sleep(500);
		assertTrue(manager.isOfflineClient(c1.getId()));
		assertTrue(manager.isOfflineClient(c2.getId()));
		assertNull(manager.pollPendingClient());

		c1.AddConnection(new WebSocketClientConnection());
		Thread.sleep(500);
		assertTrue(manager.isIdleClient(c1.getId()));
		assertTrue(manager.isOfflineClient(c2.getId()));
		assertNull(manager.pollPendingClient());

		c1.pendingMessage(new PublishMessage());
		Thread.sleep(500);
		assertFalse(manager.isIdleClient(c1.getId()));
		assertNotNull(manager.pollPendingClient());
	}
}