package com.taobao.top.push;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

public class ClientMessageSenderTest {
	@Test
	public void send_sent_test() {
		Queue<ClientConnection> queue = new ConcurrentLinkedQueue<ClientConnection>();
		queue.add(new ConnectionMock());
		queue.add(new ConnectionMock());
		ClientMessageSender sender = new ClientMessageSender(queue);
		assertTrue(sender.send(null));
		assertEquals(2, queue.size());
	}

	@Test
	public void send_when_closed_connection_test() {
		Queue<ClientConnection> queue = new ConcurrentLinkedQueue<ClientConnection>();
		ConnectionMock c1 = new ConnectionMock(false, true);
		ConnectionMock c2 = new ConnectionMock();
		queue.add(c1);
		queue.add(c2);

		ClientMessageSender sender = new ClientMessageSender(queue);
		assertTrue(sender.send(null));
		assertEquals(0, c1.sendCount);
		assertEquals(1, c2.sendCount);
		assertEquals(1, queue.size());
	}

	@Test
	public void send_when_all_closed_connection_test() {
		Queue<ClientConnection> queue = new ConcurrentLinkedQueue<ClientConnection>();
		ConnectionMock c1 = new ConnectionMock(false, true);
		ConnectionMock c2 = new ConnectionMock(false, true);
		queue.add(c1);
		queue.add(c2);

		ClientMessageSender sender = new ClientMessageSender(queue);
		assertFalse(sender.send(null));
		assertEquals(0, c1.sendCount);
		assertEquals(0, c2.sendCount);
		assertEquals(0, queue.size());
	}

	@Test
	public void send_when_send_error_test() {
		Queue<ClientConnection> queue = new ConcurrentLinkedQueue<ClientConnection>();
		ConnectionMock c1 = new ConnectionMock(true, false);
		ConnectionMock c2 = new ConnectionMock();
		queue.add(c1);
		queue.add(c2);

		ClientMessageSender sender = new ClientMessageSender(queue);
		assertTrue(sender.send(null));
		assertEquals(0, c1.sendCount);
		assertEquals(1, c2.sendCount);
		assertEquals(1, queue.size());
	}

	@Test
	public void send_when_all_send_error_test() {
		Queue<ClientConnection> queue = new ConcurrentLinkedQueue<ClientConnection>();
		ConnectionMock c1 = new ConnectionMock(true, false);
		ConnectionMock c2 = new ConnectionMock(true, false);
		queue.add(c1);
		queue.add(c2);

		ClientMessageSender sender = new ClientMessageSender(queue);
		assertFalse(sender.send(null));
		assertEquals(0, c1.sendCount);
		assertEquals(0, c2.sendCount);
		assertEquals(0, queue.size());
	}

	@Test
	public void send_drop_test() {
		final AtomicInteger counter = new AtomicInteger();
		Queue<ClientConnection> queue = new ConcurrentLinkedQueue<ClientConnection>();
		queue.add(new ConnectionMock() {
			@Override
			public SendStatus sendMessage(Object msg) throws Exception {
				return SendStatus.DROP;
			}
		});
		queue.add(new ConnectionMock() {
			@Override
			public SendStatus sendMessage(Object msg) throws Exception {
				counter.incrementAndGet();
				return SendStatus.SENT;
			}
		});

		ClientMessageSender sender = new ClientMessageSender(queue);
		assertTrue(sender.send(null));
		assertEquals(1, queue.size());
		assertEquals(0, counter.get());
	}

	@Test
	public void send_retry_test() {
		final AtomicInteger counter = new AtomicInteger();
		Queue<ClientConnection> queue = new ConcurrentLinkedQueue<ClientConnection>();
		queue.add(new ConnectionMock() {
			@Override
			public SendStatus sendMessage(Object msg) throws Exception {
				return SendStatus.RETRY;
			}
		});
		queue.add(new ConnectionMock() {
			@Override
			public SendStatus sendMessage(Object msg) throws Exception {
				counter.incrementAndGet();
				return SendStatus.SENT;
			}
		});
		ClientMessageSender sender = new ClientMessageSender(queue);
		assertTrue(sender.send(null));
		assertEquals(1, queue.size());
		assertEquals(1, counter.get());
	}

	@Test
	public void send_fail_test() {
		Queue<ClientConnection> queue = new ConcurrentLinkedQueue<ClientConnection>();
		queue.add(new ConnectionMock() {
			@Override
			public SendStatus sendMessage(Object msg) throws Exception {
				return SendStatus.RETRY;
			}
		});
		queue.add(new ConnectionMock() {
			@Override
			public SendStatus sendMessage(Object msg) throws Exception {
				return SendStatus.RETRY;
			}
		});
		ClientMessageSender sender = new ClientMessageSender(queue);
		assertFalse(sender.send(null));
		assertEquals(0, queue.size());
	}

}
