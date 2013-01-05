package com.tmall.top.push.websocket;

import static org.junit.Assert.*;

import java.nio.ByteBuffer;
import java.util.HashMap;

import org.junit.Test;

import com.tmall.top.push.MessageTooLongException;
import com.tmall.top.push.NoMessageBufferException;
import com.tmall.top.push.PushManager;
import com.tmall.top.push.UnauthorizedException;
import com.tmall.top.push.messages.Message;
import com.tmall.top.push.messages.MessageIO;
import com.tmall.top.push.messages.MessageType;
import com.tmall.top.push.websocket.WebSocketClientConnection;
import com.tmall.top.push.websocket.WebSocketClientConnectionPool;

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
		// headers.put("id", "abc");
		headers.put("origin", "abc");

		connection.init(headers, this.getManager());
		assertEquals(headers.get("origin"), connection.getOrigin());
		assertEquals(headers.get("origin"), connection.getId());

		assertFalse(connection.isOpen());
	}

	@Test(expected = UnauthorizedException.class)
	public void verify_header_test() throws UnauthorizedException {
		WebSocketClientConnection connection = this.getConnection();
		HashMap<String, String> headers = new HashMap<String, String>();
		connection.init(headers, this.getManager());
		connection.verifyHeaders();
	}

	@Test
	public void parse_test() throws MessageTooLongException,
			NoMessageBufferException {
		WebSocketClientConnection connection = this.getConnection();
		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put("id", "abc");
		connection.init(headers, this.getManager());

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
		assertEquals(connection.getId(), msg.from);
	}

	private WebSocketClientConnection getConnection() {
		WebSocketClientConnectionPool pool = this.getPool();
		return pool.acquire();
	}

	private PushManager getManager() {
		return new PushManager(10, 1024, 1, 0, 1000, 10000);
	}

	private WebSocketClientConnectionPool getPool() {
		return new WebSocketClientConnectionPool(2);
	}
}
