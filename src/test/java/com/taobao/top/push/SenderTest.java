package com.taobao.top.push;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

import org.junit.Test;

public class SenderTest {
	private LoggerFactory loggerFactory = new DefaultLoggerFactory(true, true, true, true, true);

	@Test
	public void dispatch_later_test() throws InterruptedException {
		final CountDownLatch latch = new CountDownLatch(10);
		Semaphore semaphore = new Semaphore(0);
		Sender sender = new Sender(loggerFactory, new CancellationToken(), semaphore, 1) {
			@Override
			protected Runnable createRunnable(Client pendingClient, int flushCount) {
				return new Runnable() {
					@Override
					public void run() {
						latch.countDown();
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
						}
					}
				};
			}
		};
		new Thread(sender).start();
		semaphore.release();
		latch.await();
	}
}
