package top.push;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class SimpleMessagingScheduler implements MessagingScheduler {
	private ExecutorService executor;

	public SimpleMessagingScheduler(ExecutorService executor) {
		this.executor = executor;
	}

	public SimpleMessagingScheduler() {
		this(new ThreadPoolExecutor(
				Runtime.getRuntime().availableProcessors(),
				Runtime.getRuntime().availableProcessors() * 2,
				30, TimeUnit.SECONDS,
				new ArrayBlockingQueue<Runnable>(10000),
				Executors.defaultThreadFactory(),
				new ThreadPoolExecutor.CallerRunsPolicy()));
	}

	// TODO support high water
	public void setSenderHighWater(int value) {
	}

	public void schedule(Client client, MessagingTask messaging) {
		this.executor.submit(messaging);
	}

}
