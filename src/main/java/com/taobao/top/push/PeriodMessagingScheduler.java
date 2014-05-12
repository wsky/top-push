package com.taobao.top.push;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PeriodMessagingScheduler implements MessagingScheduler {
	private static Logger logger = LoggerFactory.getLogger(PeriodMessagingScheduler.class);
	private static Random RANDOM = new Random();

	private Set<Client> pendingClients;

	private Timer timer;
	private TimerTask task;

	private ExecutorService executor;

	public PeriodMessagingScheduler() {
		this.pendingClients = new ConcurrentSkipListSet<Client>();

		this.setPeriod(100);

		this.executor = new ThreadPoolExecutor(
				Runtime.getRuntime().availableProcessors(),
				Runtime.getRuntime().availableProcessors() * 2,
				30, TimeUnit.SECONDS,
				new SynchronousQueue<Runnable>(),
				Executors.defaultThreadFactory(),
				new ThreadPoolExecutor.CallerRunsPolicy());
	}

	public void setPeriod(int value) {
		if (this.timer == null)
			this.timer = new Timer(this.getClass().getSimpleName(), true);

		if (this.task != null)
			this.task.cancel();

		this.timer.purge();
		this.timer.schedule(this.task = new TimerTask() {
			@Override
			public void run() {
				try {
					dispatch();
				} catch (Exception e) {
					logger.error("dispatch error", e);
				}

			}
		}, value, value);
	}

	public void schedule(Client client, MessagingTask messaging) {
		Queue<MessagingTask> queue = this.getQueue(client);
		if (queue == null) {
			synchronized (client) {
				if ((queue = this.getQueue(client)) == null)
					client.setContext(Queue.class,
							queue = new ArrayBlockingQueue<MessagingTask>(10000));
			}
		}
		if (!queue.offer(messaging))
			messaging.run();
		else
			this.pendingClients.add(client);
	}

	protected void dispatch() {
		Set<Client> temp = this.pendingClients;
		this.pendingClients = new ConcurrentSkipListSet<Client>();
		List<Client> clients = new ArrayList<Client>(temp);
		// avoid not fair between clients
		Collections.shuffle(clients, RANDOM);

		for (Client client : clients) {
			try {
				this.execute(client);
			} catch (Exception e) {
				logger.error("dispatch error: " + client.getId(), e);
			}
		}
	}

	protected void execute(final Client client) {
		this.executor.submit(new Runnable() {
			@Override
			public void run() {
				try {
					flush(client);
				} catch (Exception e) {
					logger.error("flush error: " + client.getId(), e);
				}
			}
		});
	}

	protected void flush(Client client) {
		Queue<MessagingTask> queue = this.getQueue(client);
		MessagingTask messaging = null;

		while ((messaging = queue.poll()) != null) {
			try {
				if (!this.isCompleted(messaging.execute()))
					break;
			} catch (Exception e) {
				logger.error("messaging error", e);
			}
		}
	}

	protected boolean isCompleted(MessagingStatus status) {
		return status == MessagingStatus.SENT || status == MessagingStatus.ABORT;
	}

	@SuppressWarnings("unchecked")
	private Queue<MessagingTask> getQueue(Client client) {
		return (Queue<MessagingTask>) client.getContext(Queue.class);
	}
}
