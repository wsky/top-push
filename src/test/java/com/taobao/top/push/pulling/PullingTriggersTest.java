package com.taobao.top.push.pulling;

import static org.junit.Assert.*;

import java.util.concurrent.CountDownLatch;

import org.junit.Test;

public class PullingTriggersTest {
	private static Object request = "pull_request";

	@Test
	public void delay_test() throws InterruptedException {
		final CountDownLatch latch = new CountDownLatch(1);
		PullingTriggers triggers = new PullingTriggers() {
			@Override
			protected void dispatch(Object trigger) {
				latch.countDown();

			}
		};
		long begin = System.currentTimeMillis();
		assertTrue(triggers.delayTrigger(request, 500));
		latch.await();
		assertTrue(System.currentTimeMillis() - begin >= 500);
	}

	@Test
	public void delay_zero_test() throws InterruptedException {
		final CountDownLatch latch = new CountDownLatch(2);
		PullingTriggers triggers = new PullingTriggers() {
			@Override
			protected void dispatch(Object trigger) {
				latch.countDown();

			}
		};
		assertTrue(triggers.delayTrigger(request, 0));
		assertTrue(triggers.delayTrigger(request, -100));
		latch.await();
	}

	@Test
	public void many_trigger_test() throws InterruptedException {
		int total = 10000;
		final CountDownLatch latch = new CountDownLatch(total);
		PullingTriggers triggers = new PullingTriggers() {
			@Override
			protected void dispatch(Object trigger) {
				latch.countDown();
			}
		};
		triggers.setMaxTriggerCount(total);

		System.out.println(Runtime.getRuntime().totalMemory());
		for (int i = 0; i < total; i++)
			assertTrue(triggers.delayTrigger(request, 100));
		System.out.println(Runtime.getRuntime().totalMemory());

		assertFalse(triggers.delayTrigger(request, 100));

		latch.await();
		System.out.println(Runtime.getRuntime().totalMemory());
	}
}