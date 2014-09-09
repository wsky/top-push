package top.push;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import top.push.MessagingScheduler;
import top.push.MessagingStatus;
import top.push.MessagingTask;
import top.push.SimpleMessagingScheduler;

public class SimpleMessagingSchedulerTest {
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
		MessagingScheduler scheduler = new SimpleMessagingScheduler(new ThreadPoolExecutor(
				count, count,
				30, TimeUnit.SECONDS,
				workQueue,
				Executors.defaultThreadFactory(),
				policy));

		MessagingTask task = new MessagingTask() {
			@Override
			public MessagingStatus execute() {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
				return MessagingStatus.SENT;
			}
		};

		for (int i = 0; i < count; i++)
			scheduler.schedule(null, task);

		scheduler.schedule(null, task);
	}
}
