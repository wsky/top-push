package com.taobao.top.push;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MessageSender {
	private ExecutorService executor;

	public MessageSender() {
		this.executor = new ThreadPoolExecutor(
				4, 8,
				300, TimeUnit.SECONDS,
				new SynchronousQueue<Runnable>(),
				Executors.defaultThreadFactory(),
				new ThreadPoolExecutor.CallerRunsPolicy());
	}

	public void send(Object message) {
		this.executor.submit(new Runnable() {
			@Override
			public void run() {
			}
		});
	}
}
