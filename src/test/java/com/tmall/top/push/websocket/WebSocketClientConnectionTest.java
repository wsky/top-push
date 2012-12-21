package com.tmall.top.push.websocket;

import static org.junit.Assert.*;

import java.util.HashMap;

import org.junit.Test;

import com.tmall.top.push.PushManager;
import com.tmall.top.push.UnauthorizedException;
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
		headers.put("id", "abc");
		headers.put("Origin", "localhost");
		connection.init(headers, this.getManager());
		assertEquals(headers.get("id"), connection.getId());
		assertEquals(headers.get("origin"), connection.getOrigin());

		assertFalse(connection.isOpen());
	}

	@Test(expected = UnauthorizedException.class)
	public void verify_header_test() throws UnauthorizedException {
		WebSocketClientConnection connection = this.getConnection();
		HashMap<String, String> headers = new HashMap<String, String>();
		connection.init(headers, this.getManager());
		connection.verifyHeaders();
	}

	private WebSocketClientConnection getConnection() {
		WebSocketClientConnectionPool pool = this.getPool();
		return pool.acquire();
	}

	private PushManager getManager() {
		return new PushManager(10, 1024, 1024, 1, 1, 0, 1000, 10000);
	}

	private WebSocketClientConnectionPool getPool() {
		return new WebSocketClientConnectionPool(2);
	}
}
