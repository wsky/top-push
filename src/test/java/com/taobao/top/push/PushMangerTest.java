package com.taobao.top.push;

import static org.junit.Assert.*;

import org.junit.Test;

import com.taobao.top.push.PushManager;

public class PushMangerTest {
	@Test
	public void get_client_test() throws Exception {
		PushManager manager = new PushManager();
		Object id = "abc";
		assertNull(manager.getClient(id));
		manager.getOrCreateClient(id);
		assertNotNull(manager.getClient(id));
	}

	@Test
	public void clients_perf_test() {
		PushManager manager = new PushManager();
		int total = 100000;
		for (int i = 0; i < total; i++)
			manager.getOrCreateClient(this.parseId(i));

		long begin = System.currentTimeMillis();
		for (int i = 0; i < total; i++)
			manager.getClient(this.parseId(i));
		System.out.println(System.currentTimeMillis() - begin);
	}

	private Object parseId(int i) {
		return i;// Integer.toString(i)
	}
}