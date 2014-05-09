package com.taobao.top.push;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.junit.Test;

public class ClientMessageSenderTest {
	@Test
public void send_sent_test() {
	List<ClientConnection> connections = new ArrayList<ClientConnection>();
	connections.add(new ConnectionMock());
	queue.add(new ConnectionMock());
	ClientMessageSender sender =new ClientMessageSender(connections);
	assertTrue(sender.send(null));
	assertEquals(2, queue.size());	
}
}
