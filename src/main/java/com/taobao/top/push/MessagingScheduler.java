package com.taobao.top.push;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MessagingScheduler {
	private ExecutorService executor;

	public MessagingScheduler(ExecutorService executor) {
		this.executor = executor;
	}

	public MessagingScheduler() {
		this(new ThreadPoolExecutor(
				Runtime.getRuntime().availableProcessors(),
				Runtime.getRuntime().availableProcessors() * 2,
				30, TimeUnit.SECONDS,
				new ArrayBlockingQueue<Runnable>(10000),
				Executors.defaultThreadFactory(),
				new ThreadPoolExecutor.CallerRunsPolicy()));
	}
	
	public void setSenderHighWater(int value) {

	}

	public void schedule(Client client, Runnable messaging) {
		this.executor.submit(messaging);
	}
}
