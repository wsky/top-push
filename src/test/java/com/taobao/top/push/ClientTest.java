package com.taobao.top.push;

import static org.junit.Assert.*;

import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.junit.Test;

import com.taobao.top.push.CancellationToken;
import com.taobao.top.push.Client;
import com.taobao.top.push.ClientConnection;

public class ClientTest {
	static int sendCount = 0;

	@Test
	public void init_test() {
		Object id = new DefaultIdentity("abc");
		Client client = new Client(new DefaultLoggerFactory(), id);
		assertEquals(id, client.getId());
		assertEquals(0, client.getConnectionsCount());
		assertEquals(0, client.getPendingMessagesCount());
	}

	@Test
	public void add_remove_connection_test() {
		Client client = new Client(new DefaultLoggerFactory(), new DefaultIdentity("abc"));
		ConnectionWrapper c1 = new ConnectionWrapper();
		ConnectionWrapper c2 = new ConnectionWrapper();
		client.AddConnection(c1);
		client.AddConnection(c2);
		assertEquals(2, client.getConnectionsCount());
		assertEquals(2, client.getConnectionQueueSize());
		client.RemoveConnection(c1);
		assertEquals(1, client.getConnectionsCount());
		assertEquals(1, client.getConnectionQueueSize());
	}

	@Test
	public void pending_message_test() {
		Client client = new Client(new DefaultLoggerFactory(), new DefaultIdentity("abc"));
		client.pendingMessage(new Object());
		assertEquals(1, client.getPendingMessagesCount());
	}

	@Test
	public void message_state_after_flush_test() throws InterruptedException {
		final CountDownLatch latch = new CountDownLatch(1);
		Client client = new Client(new DefaultLoggerFactory(), new DefaultIdentity("abc"), new MessageStateHandler() {
			@Override
			public void onSent(Object client, Object message) {
			}

			@Override
			public void onDropped(Object client, Object message, String reason) {
				latch.countDown();
			}
		}, null);
		client.pendingMessage(new Object());
		client.flush(new CancellationToken(), 10);
		latch.await();
	}

	@Test
	public void flush_LRU_test() {
		Client client = new Client(new DefaultLoggerFactory(), new DefaultIdentity("abc"));
		ConnectionWrapper c1 = new ConnectionWrapper();
		ConnectionWrapper c2 = new ConnectionWrapper();
		client.AddConnection(c1);
		client.AddConnection(c2);
		assertEquals(2, client.getConnectionsCount());
		assertEquals(2, client.getConnectionQueueSize());

		int count = 10;
		for (int i = 0; i < count; i++) {
			client.pendingMessage(new Object());
		}

		client.flush(new CancellationToken(), count);
		assertEquals(count, sendCount);
		assertEquals(count / 2, c1.sendCount);
		assertEquals(count / 2, c2.sendCount);
		assertEquals(2, client.getConnectionsCount());
		assertEquals(2, client.getConnectionQueueSize());
	}

	@Test
	public void flush_closed_connection_test() {
		Client client = new Client(new DefaultLoggerFactory(), new DefaultIdentity("abc"));
		ConnectionWrapper c1 = new ConnectionWrapper(false, true);
		ConnectionWrapper c2 = new ConnectionWrapper();
		client.AddConnection(c1);
		client.AddConnection(c2);
		client.pendingMessage(new Object());
		client.flush(new CancellationToken(), 1);
		assertEquals(0, c1.sendCount);
		assertEquals(1, c2.sendCount);
		assertEquals(1, client.getConnectionsCount());
		assertEquals(1, client.getConnectionQueueSize());
	}

	@Test
	public void flush_when_send_error_test() {
		Client client = new Client(new DefaultLoggerFactory(), new DefaultIdentity("abc"));
		ConnectionWrapper c1 = new ConnectionWrapper(true, false);
		ConnectionWrapper c2 = new ConnectionWrapper();
		client.AddConnection(c1);
		client.AddConnection(c2);
		client.pendingMessage(new Object());
		client.flush(new CancellationToken(), 1);
		assertEquals(0, c1.sendCount);
		assertEquals(0, c2.sendCount);
		// only remove by server container
		assertEquals(2, client.getConnectionsCount());
		assertEquals(2, client.getConnectionQueueSize());
	}

	@Test
	public void connection_state_test() throws InterruptedException {
		final CountDownLatch latch = new CountDownLatch(1);
		Client client = new Client(new DefaultLoggerFactory(),
				new DefaultIdentity("abc"), null,
				new ClientStateHandler() {
					@Override
					public void onClientPending(Client client) {
					}

					@Override
					public void onClientOffline(Client client) {
					}

					@Override
					public void onClientIdle(Client client) {
					}

					@Override
					public void onClientDisconnect(Client client, ClientConnection clientConnection) {
						latch.countDown();
					}

					@Override
					public Object onClientConnecting(Map<String, String> headers) throws Exception {
						return null;
					}
				});
		client.pendingMessage(new Object());
		client.AddConnection(new ConnectionWrapper(false, false));
		client.flush(new CancellationToken(), 10);
		latch.await();
	}
}
