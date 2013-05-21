package com.taobao.top.push.websocket;

import static org.junit.Assert.*;

import java.nio.ByteBuffer;
import java.util.HashMap;

import org.junit.Test;

import com.taobao.top.push.DefaultIdentity;
import com.taobao.top.push.messages.Message;
import com.taobao.top.push.messages.MessageIO;
import com.taobao.top.push.messages.MessageType;
import com.taobao.top.push.websocket.WebSocketClientConnection;
import com.taobao.top.push.websocket.WebSocketClientConnectionPool;

public class WebSocketClientConnectionTest {
	@Test
	public void pool_test() {
		WebSocketClientConnectionPool pool = this.getPool();
		WebSocketClientConnection connection = pool.acquire();
		connection.clear();
		pool.release(connection);
		assertEquals(connection, pool.acquire());
	}

	@Test
	public void init_header_test() {
		WebSocketClientConnection connection = this.getConnection();
		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put("origin", "abc");

		connection.init(new DefaultIdentity("abc"), headers);
		assertEquals(headers.get("origin"), connection.getOrigin());
		assertEquals(new DefaultIdentity("abc"), connection.getId());

		assertFalse(connection.isOpen());
	}

	@Test
	public void parse_test() throws MessageTooLongException,
			NoMessageBufferException {
		WebSocketClientConnection connection = this.getConnection();
		HashMap<String, String> headers = new HashMap<String, String>();
		connection.init(new DefaultIdentity("abc"), headers);
		connection.init(null, new Receiver(1024, 1));

		byte[] bytes = new byte[1024];
		ByteBuffer buffer = ByteBuffer.wrap(bytes);
		Message msgPublish = new Message();
		msgPublish.messageType = MessageType.PUBLISH;
		msgPublish.to = "abc";
		msgPublish.remainingLength = 100;
		MessageIO.parseClientSending(msgPublish, buffer);

		Message msg = connection.parse(bytes, 0, 1024);
		assertEquals(Message.class, msg.getClass());
		// message from must be fill after connection received
		assertEquals(connection.getId(), new DefaultIdentity(msg.from));
	}

	private WebSocketClientConnection getConnection() {
		WebSocketClientConnectionPool pool = this.getPool();
		return pool.acquire();
	}

	private WebSocketClientConnectionPool getPool() {
		return new WebSocketClientConnectionPool(2);
	}
}
