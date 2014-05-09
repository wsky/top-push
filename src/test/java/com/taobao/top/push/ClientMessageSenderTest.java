package com.taobao.top.push;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class ClientMessageSenderTest {
	@Test
	public void send_test() {
		ConnectionMock c1 = new ConnectionMock();
		ConnectionMock c2 = new ConnectionMock();
		ClientMessageSender sender = new ClientMessageSender();
		sender.setConnections(new ClientConnection[] { c1, c2 });

		assertEquals(MessagingStatus.SENT, sender.send(null));
		assertEquals(1, c1.sendCount);
		assertEquals(0, c2.sendCount);

		assertEquals(MessagingStatus.SENT, sender.send(null));
		assertEquals(1, c1.sendCount);
		assertEquals(1, c2.sendCount);
	}

	@Test
	public void send_fail_test() {
		ConnectionMock c1 = new ConnectionMock(false, false);
		ConnectionMock c2 = new ConnectionMock(false, true);
		ClientMessageSender sender = new ClientMessageSender();
		sender.setConnections(new ClientConnection[] { c1, c2 });

		assertEquals(MessagingStatus.FAULT, sender.send(null));
		assertEquals(0, c1.sendCount);
		assertEquals(0, c2.sendCount);
	}

	@Test
	public void send_and_index_reset_to_zero_test() {
		List<ClientConnection> connections = new ArrayList<ClientConnection>();
		ConnectionMock c1 = new ConnectionMock();
		ConnectionMock c2 = new ConnectionMock(false, false);
		ConnectionMock c3 = new ConnectionMock(false, false);
		connections.add(c1);
		connections.add(c2);
		connections.add(c3);
		ClientMessageSender sender = new ClientMessageSender();
		sender.setConnections(new ClientConnection[] { c1, c2, c3 });

		// begin=0, i=0
		assertEquals(MessagingStatus.SENT, sender.send(null));
		// begin=1, i=0
		assertEquals(MessagingStatus.SENT, sender.send(null));
		// begin=2, i=0
		assertEquals(MessagingStatus.SENT, sender.send(null));

		assertEquals(3, c1.sendCount);
		assertEquals(0, c2.sendCount);
		assertEquals(0, c3.sendCount);
	}
}
