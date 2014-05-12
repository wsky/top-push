package com.taobao.top.push.pulling;

import static org.junit.Assert.*;

import org.junit.Test;

public class PullRequestPendingsTest {
	@Test
	public void pending_test() {
		PullRequestPendings pendings = new PullRequestPendings();
		assertTrue(pendings.setPending(1));
		assertFalse(pendings.setPending(1));
		pendings.cancelPending(1);
		assertTrue(pendings.setPending(1));
	}

	@Test
	public void timeout_test() throws InterruptedException {
		PullRequestPendings pendings = new PullRequestPendings();
		pendings.setTimeout(100);
		assertTrue(pendings.setPending(1));
		assertFalse(pendings.setPending(1));
		Thread.sleep(500);
		assertTrue(pendings.setPending(1));
	}
}
