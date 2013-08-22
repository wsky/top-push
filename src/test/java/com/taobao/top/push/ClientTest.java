package com.taobao.top.push;

import static org.junit.Assert.*;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

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
		client.RemoveConnection(c1);
		assertEquals(1, client.getConnectionsCount());
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

	// change to random, not LRU
	// @Test
	public void flush_LRU_test() {
		Client client = new Client(new DefaultLoggerFactory(), new DefaultIdentity("abc"));
		ConnectionWrapper c1 = new ConnectionWrapper();
		ConnectionWrapper c2 = new ConnectionWrapper();
		client.AddConnection(c1);
		client.AddConnection(c2);
		assertEquals(2, client.getConnectionsCount());

		int count = 10;
		for (int i = 0; i < count; i++) {
			client.pendingMessage(new Object());
		}

		client.flush(new CancellationToken(), count);
		assertEquals(count, sendCount);
		assertEquals(count / 2, c1.sendCount);
		assertEquals(count / 2, c2.sendCount);
		assertEquals(2, client.getConnectionsCount());
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

	@Test
	public void flush_loop_test() {
		final AtomicBoolean flag = new AtomicBoolean();
		Client client = new Client(new DefaultLoggerFactory(), new DefaultIdentity("abc")) {
			@Override
			protected boolean SendMessage(CancellationToken token, Object message, java.util.Queue<ClientConnection> connectionQueue) {
				return flag.get();
			};
		};
		client.pendingMessage(new Object());
		client.pendingMessage(new Object());
		client.pendingMessage(new Object());
		client.pendingMessage(new Object());

		flag.set(true);
		client.flush(new CancellationToken(), 2);
		assertEquals(2, client.getTotalSendMessageCount());

		flag.set(false);
		client.flush(new CancellationToken(), 2);
		assertEquals(2, client.getTotalSendMessageCount());
	}

	@Test
	public void send_when_closed_connection_test() {
		Queue<ClientConnection> queue = new ConcurrentLinkedQueue<ClientConnection>();
		ConnectionWrapper c1 = new ConnectionWrapper(false, true);
		ConnectionWrapper c2 = new ConnectionWrapper();
		queue.add(c1);
		queue.add(c2);

		Client client = new Client(new DefaultLoggerFactory(), new DefaultIdentity("abc"));
		client.AddConnection(c1);
		client.AddConnection(c2);

		client.SendMessage(new CancellationToken(), new Object(), queue);
		assertEquals(0, c1.sendCount);
		assertEquals(1, c2.sendCount);
		assertEquals(1, client.getConnectionsCount());
		assertEquals(1, queue.size());
	}

	@Test
	public void send_when_all_closed_connection_test() {
		Queue<ClientConnection> queue = new ConcurrentLinkedQueue<ClientConnection>();
		ConnectionWrapper c1 = new ConnectionWrapper(false, true);
		ConnectionWrapper c2 = new ConnectionWrapper(false, true);
		queue.add(c1);
		queue.add(c2);

		Client client = new Client(new DefaultLoggerFactory(), new DefaultIdentity("abc"));
		client.AddConnection(c1);
		client.AddConnection(c2);

		client.SendMessage(new CancellationToken(), new Object(), queue);
		assertEquals(0, c1.sendCount);
		assertEquals(0, c2.sendCount);
		assertEquals(0, client.getConnectionsCount());
		assertEquals(0, queue.size());
	}

	@Test
	public void send_when_send_error_test() {
		Queue<ClientConnection> queue = new ConcurrentLinkedQueue<ClientConnection>();
		ConnectionWrapper c1 = new ConnectionWrapper(true, false);
		ConnectionWrapper c2 = new ConnectionWrapper();
		queue.add(c1);
		queue.add(c2);

		Client client = new Client(new DefaultLoggerFactory(), new DefaultIdentity("abc"));
		client.AddConnection(c1);
		client.AddConnection(c2);

		client.SendMessage(new CancellationToken(), new Object(), queue);
		assertEquals(0, c1.sendCount);
		assertEquals(1, c2.sendCount);
		// only removed when isOpen=false or by server host
		assertEquals(2, client.getConnectionsCount());
		assertEquals(1, queue.size());
	}

	@Test
	public void send_when_all_send_error_test() {
		Queue<ClientConnection> queue = new ConcurrentLinkedQueue<ClientConnection>();
		ConnectionWrapper c1 = new ConnectionWrapper(true, false);
		ConnectionWrapper c2 = new ConnectionWrapper(true, false);
		queue.add(c1);
		queue.add(c2);

		Client client = new Client(new DefaultLoggerFactory(), new DefaultIdentity("abc"));
		client.AddConnection(c1);
		client.AddConnection(c2);

		client.SendMessage(new CancellationToken(), new Object(), queue);
		assertEquals(0, c1.sendCount);
		assertEquals(0, c2.sendCount);
		assertEquals(2, client.getConnectionsCount());
		assertEquals(0, queue.size());
	}

	@Test
	public void send_sent_test() {
		Queue<ClientConnection> queue = new ConcurrentLinkedQueue<ClientConnection>();
		queue.add(new ConnectionWrapper());
		queue.add(new ConnectionWrapper());
		Client client = new Client(new DefaultLoggerFactory(), new DefaultIdentity("abc"));
		assertTrue(client.SendMessage(new CancellationToken(), new Object(), queue));
		assertEquals(2, queue.size());
	}

	@Test
	public void send_drop_test() {
		final AtomicInteger counter = new AtomicInteger();
		Queue<ClientConnection> queue = new ConcurrentLinkedQueue<ClientConnection>();
		queue.add(new ConnectionWrapper() {
			@Override
			public SendStatus sendMessage(Object msg) throws Exception {
				return SendStatus.DROP;
			}
		});
		queue.add(new ConnectionWrapper() {
			@Override
			public SendStatus sendMessage(Object msg) throws Exception {
				counter.incrementAndGet();
				return SendStatus.SENT;
			}
		});
		Client client = new Client(new DefaultLoggerFactory(), new DefaultIdentity("abc"));
		assertTrue(client.SendMessage(new CancellationToken(), new Object(), queue));
		assertEquals(1, queue.size());
		assertEquals(0, counter.get());
	}

	@Test
	public void send_retry_test() {
		final AtomicInteger counter = new AtomicInteger();
		Queue<ClientConnection> queue = new ConcurrentLinkedQueue<ClientConnection>();
		queue.add(new ConnectionWrapper() {
			@Override
			public SendStatus sendMessage(Object msg) throws Exception {
				return SendStatus.RETRY;
			}
		});
		queue.add(new ConnectionWrapper() {
			@Override
			public SendStatus sendMessage(Object msg) throws Exception {
				counter.incrementAndGet();
				return SendStatus.SENT;
			}
		});
		Client client = new Client(new DefaultLoggerFactory(), new DefaultIdentity("abc"));
		assertTrue(client.SendMessage(new CancellationToken(), new Object(), queue));
		assertEquals(1, queue.size());
		assertEquals(1, counter.get());
	}

	@Test
	public void send_fail_test() {
		Queue<ClientConnection> queue = new ConcurrentLinkedQueue<ClientConnection>();
		queue.add(new ConnectionWrapper() {
			@Override
			public SendStatus sendMessage(Object msg) throws Exception {
				return SendStatus.RETRY;
			}
		});
		queue.add(new ConnectionWrapper() {
			@Override
			public SendStatus sendMessage(Object msg) throws Exception {
				return SendStatus.RETRY;
			}
		});
		Client client = new Client(new DefaultLoggerFactory(), new DefaultIdentity("abc"));
		assertFalse(client.SendMessage(new CancellationToken(), new Object(), queue));
		assertEquals(0, queue.size());
	}
}
