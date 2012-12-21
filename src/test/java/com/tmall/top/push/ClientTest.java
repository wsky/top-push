package com.tmall.top.push;

import static org.junit.Assert.*;

import org.junit.Test;

import com.tmall.top.push.messages.Message;
import com.tmall.top.push.messages.PublishMessage;

public class ClientTest {
	static int sendCount = 0;

	@Test
	public void init_test() {
		Client client = new Client("abc", this.getManager());
		assertEquals("abc", client.getId());
		assertEquals(0, client.getConnectionsCount());
		assertEquals(0, client.getPendingMessagesCount());
	}

	@Test
	public void add_remove_connection_test() {
		Client client = new Client("abc", this.getManager());
		TestConnection c1 = new TestConnection();
		TestConnection c2 = new TestConnection();
		client.AddConnection(c1);
		client.AddConnection(c2);
		assertEquals(2, client.getConnectionsCount());
		client.RemoveConnection(c1);
		assertEquals(1, client.getConnectionsCount());
	}

	@Test
	public void pending_message_test() {
		Client client = new Client("abc", this.getManager());
		client.pendingMessage(new PublishMessage());
		assertEquals(1, client.getPendingMessagesCount());
	}

	@Test
	public void release_after_send_message_test() {
		Client client = new Client("abc", this.getManager());
		PublishMessage msg = new PublishMessage();
		msg.body = new Object();
		client.pendingMessage(msg);
		client.flush(new CancellationToken(), 10);
		// message should be release to pool whatever after send
		assertNull(msg.body);
	}

	@Test
	public void flush_fifo_test() {
		Client client = new Client("abc", this.getManager());
		TestConnection c1 = new TestConnection();
		TestConnection c2 = new TestConnection();
		client.AddConnection(c1);
		client.AddConnection(c2);
		assertEquals(2, client.getConnectionsCount());
		assertEquals(2, client.getConnectionQueueSize());

		int count = 10;
		for (int i = 0; i < count; i++) {
			client.pendingMessage(new PublishMessage());
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
		Client client = new Client("abc", this.getManager());
		TestConnection c1 = new TestConnection(false, true);
		TestConnection c2 = new TestConnection();
		client.AddConnection(c1);
		client.AddConnection(c2);
		client.pendingMessage(new PublishMessage());
		client.flush(new CancellationToken(), 1);
		assertEquals(0, c1.sendCount);
		assertEquals(1, c2.sendCount);
		// only remove by server container
		assertEquals(2, client.getConnectionsCount());
		assertEquals(1, client.getConnectionQueueSize());
	}

	@Test
	public void flush_when_send_error_test() {
		Client client = new Client("abc", this.getManager());
		TestConnection c1 = new TestConnection(true, false);
		TestConnection c2 = new TestConnection();
		client.AddConnection(c1);
		client.AddConnection(c2);
		client.pendingMessage(new PublishMessage());
		client.flush(new CancellationToken(), 1);
		assertEquals(0, c1.sendCount);
		assertEquals(0, c2.sendCount);
		// only remove by server container
		assertEquals(2, client.getConnectionsCount());
		assertEquals(2, client.getConnectionQueueSize());
	}

	private PushManager getManager() {
		return new PushManager(1024, 1024, 1, 1, 0, 1000, 10000);
	}

	public class TestConnection extends ClientConnection {
		public int sendCount;
		private boolean isOpen;
		private boolean canSend;

		public TestConnection() {
			this(true, true);
		}

		public TestConnection(boolean isOpen, boolean canSend) {
			this.isOpen = isOpen;
			this.canSend = canSend;
		}

		@Override
		protected void initHeaders() {
		}

		@Override
		protected void internalClear() {
		}

		@Override
		public boolean isOpen() {
			return this.isOpen;
		}

		@Override
		public void sendMessage(Message msg) throws java.lang.Exception {
			if (!this.canSend)
				throw new java.lang.Exception();
			this.sendCount++;
			ClientTest.sendCount++;
		}

	}
}
