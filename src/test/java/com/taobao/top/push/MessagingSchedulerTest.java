package com.taobao.top.push;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

public class MessagingSchedulerTest {
	@Test(expected = RejectedExecutionException.class)
	public void fixed_thread_and_rejected_test() {
		this.send_test(4, new SynchronousQueue<Runnable>(), new ThreadPoolExecutor.AbortPolicy());
	}

	@Test
	public void fixed_thread_and_pending_test() {
		this.send_test(4, new ArrayBlockingQueue<Runnable>(10), new ThreadPoolExecutor.AbortPolicy());
	}

	@Test
	public void fixed_thread_and_caller_runs_test() {
		this.send_test(4, new SynchronousQueue<Runnable>(), new ThreadPoolExecutor.CallerRunsPolicy());
	}

	private void send_test(int count, BlockingQueue<Runnable> workQueue, RejectedExecutionHandler policy) {
		MessagingScheduler scheduler = new MessagingScheduler(new ThreadPoolExecutor(
				count, count,
				30, TimeUnit.SECONDS,
				workQueue,
				Executors.defaultThreadFactory(),
				policy));

		Runnable r = new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
			}
		};

		for (int i = 0; i < count; i++)
			scheduler.schedule(null, r);

		scheduler.schedule(null, r);
	}
}
