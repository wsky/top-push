package top.push;

import static org.junit.Assert.*;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import top.push.BatchedMessageSender;
import top.push.ClientConnection;
import top.push.MessagingStatus;
import top.push.SendStatus;

public class BatchedMessageSenderTest {
	@Test
	public void send_sent_test() {
		Queue<ClientConnection> queue = new ConcurrentLinkedQueue<ClientConnection>();
		queue.add(new ConnectionMock());
		queue.add(new ConnectionMock());
		BatchedMessageSender sender = new BatchedMessageSender(queue);
		assertEquals(MessagingStatus.SENT, sender.send(null));
		assertEquals(2, queue.size());
	}

	@Test
	public void send_when_closed_connection_test() {
		Queue<ClientConnection> queue = new ConcurrentLinkedQueue<ClientConnection>();
		ConnectionMock c1 = new ConnectionMock(false, true);
		ConnectionMock c2 = new ConnectionMock();
		queue.add(c1);
		queue.add(c2);

		BatchedMessageSender sender = new BatchedMessageSender(queue);
		assertEquals(MessagingStatus.SENT, sender.send(null));
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

		BatchedMessageSender sender = new BatchedMessageSender(queue);
		assertEquals(MessagingStatus.FAULT, sender.send(null));
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

		BatchedMessageSender sender = new BatchedMessageSender(queue);
		assertEquals(MessagingStatus.SENT, sender.send(null));
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

		BatchedMessageSender sender = new BatchedMessageSender(queue);
		assertEquals(MessagingStatus.FAULT, sender.send(null));
		assertEquals(0, c1.sendCount);
		assertEquals(0, c2.sendCount);
		assertEquals(0, queue.size());
	}

	@Test
	public void send_in_doubt_test() {
		final AtomicInteger counter = new AtomicInteger();
		Queue<ClientConnection> queue = new ConcurrentLinkedQueue<ClientConnection>();
		queue.add(new ConnectionMock() {
			@Override
			public SendStatus sendMessage(Object msg) throws Exception {
				return SendStatus.IN_DOUBT;
			}
		});
		queue.add(new ConnectionMock() {
			@Override
			public SendStatus sendMessage(Object msg) throws Exception {
				counter.incrementAndGet();
				return SendStatus.SENT;
			}
		});

		BatchedMessageSender sender = new BatchedMessageSender(queue);
		assertEquals(MessagingStatus.IN_DOUBT, sender.send(null));
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
		BatchedMessageSender sender = new BatchedMessageSender(queue);
		assertEquals(MessagingStatus.SENT, sender.send(null));
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
		BatchedMessageSender sender = new BatchedMessageSender(queue);
		assertEquals(MessagingStatus.FAULT, sender.send(null));
		assertEquals(0, queue.size());
	}

}
