package com.taobao.top.push;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

public class PeriodMessagingScheduler implements MessagingScheduler {
	public void schedule(Client client, MessagingTask messaging) {
		Queue<MessagingTask> queue = this.getQueue(client);
		if (queue == null) {
			synchronized (client) {
				if ((queue = this.getQueue(client)) == null)
					client.setContext(Queue.class,
							queue = new ArrayBlockingQueue<MessagingTask>(10000));
			}
		}
	}

	@SuppressWarnings("unchecked")
	private Queue<MessagingTask> getQueue(Client client) {
		return (Queue<MessagingTask>) client.getContext(Queue.class);
	}
}
