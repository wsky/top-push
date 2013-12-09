package com.taobao.top.push;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

import org.junit.Test;

public class SenderTest {
	private LoggerFactory loggerFactory = new DefaultLoggerFactory(true, true, true, true, true);

	@Test
	public void dispatch_later_test() throws InterruptedException {
		int total = 10;
		final CountDownLatch latch = new CountDownLatch(total);

		Semaphore semaphore = new Semaphore(0);
		CancellationToken token = new CancellationToken();

		Sender sender = new Sender(loggerFactory, token, semaphore, 1) {
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
		for (int i = 0; i < total; i++)
			sender.pendingClient(new Client(loggerFactory, i));
		Thread thread = new Thread(sender);
		thread.start();
		semaphore.release();
		latch.await();
		token.stop();
		thread.join();
	}
}
