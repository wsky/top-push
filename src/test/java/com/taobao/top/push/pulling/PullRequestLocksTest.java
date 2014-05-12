package com.taobao.top.push.pulling;

import static org.junit.Assert.*;

import org.junit.Test;

import com.taobao.top.push.Client;

public class PullRequestLocksTest {
	private Client client = new Client(1);

	@Test
	public void acquire_test() {
		PullRequestLocks pendings = new PullRequestLocks();
		assertTrue(pendings.acquire(client, 1));
		assertFalse(pendings.acquire(client, 1));
		pendings.release(client, 1);
		assertTrue(pendings.acquire(client, 1));
	}

	@Test
	public void timeout_test() throws InterruptedException {
		PullRequestLocks pendings = new PullRequestLocks();
		pendings.setTimeout(100);
		assertTrue(pendings.acquire(client, 1));
		assertFalse(pendings.acquire(client, 1));
		Thread.sleep(500);
		assertTrue(pendings.acquire(client, 1));
	}
}
