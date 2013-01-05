package com.tmall.top.push.websocket;

import static org.junit.Assert.*;

import java.util.HashMap;

import org.junit.Test;

import com.alibaba.fastjson.JSON;
import com.tmall.top.push.PushManager;

public class RPCTest {
	@Test
	public void fast_json_test() {
		Request r1 = new Request();
		r1.Command = "isOnline";
		r1.Arguments = new HashMap<String, String>();
		r1.Arguments.put("id", "abc");

		String json = JSON.toJSONString(r1);
		System.out.println(json);
		Request r2 = JSON.parseObject(json, Request.class);
		assertEquals(r1.Command, r2.Command);
		assertEquals(r1.Arguments.get("id"), r2.Arguments.get("id"));
	}

	@Test
	public void process_request_test() {
		Request request = new Request();
		request.Command = "isOnline";
		request.Arguments = new HashMap<String, String>();
		request.Arguments.put("id", "abc");
		Response response = Utils.processRequest(JSON.toJSONString(request),
				new TestPushManager());
		assertFalse(response.IsError);
		assertEquals("true", response.Result);
	}

	@Test
	public void process_request_error_test() {
		Request request = new Request();
		request.Command = "test";
		Response response = Utils.processRequest(JSON.toJSONString(request),
				new TestPushManager());
		assertTrue(response.IsError);
		System.out.println(response.ErrorPhrase);
	}

	public class TestPushManager extends PushManager {
		public TestPushManager() {
			this(10, 1024, 10, 0, 1000, 5000);
		}

		public TestPushManager(int maxConnectionCount, int maxMessageSize,
				int maxMessageBufferCount, int senderCount, int senderIdle,
				int stateBuilderIdle) {
			super(maxConnectionCount, maxMessageSize, maxMessageBufferCount,
					senderCount, senderIdle, stateBuilderIdle);
		}

		@Override
		public boolean isOnlineClient(String id) {
			return true;
		}

	}
}
