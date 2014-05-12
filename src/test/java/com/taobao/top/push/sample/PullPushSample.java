package com.taobao.top.push.sample;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.LoggerFactory;

import org.slf4j.Logger;

import com.taobao.top.push.Client;
import com.taobao.top.push.ClientConnection;
import com.taobao.top.push.PushManager;
import com.taobao.top.push.pulling.PullRequestScheduler;
import com.taobao.top.push.pulling.PullingTriggers;

public class PullPushSample {
	static Logger logger = LoggerFactory.getLogger(PullPushSample.class);
	static PushManager manager;
	static PullingTriggers triggers;
	static PullRequestScheduler scheduler;
	static Object clientId = "client-test";

	public static void main(String[] args) throws IOException, InterruptedException {
		manager = new PushManager();

		triggers = new PullingTriggers() {
			@Override
			protected void dispatch(Object trigger) {
				scheduler.dispatch(manager.getClient(trigger), trigger);
			}
		};

		scheduler = new PullRequestScheduler() {
			@Override
			protected void pull(Object request, Client client, int amount, Callback callback) {
				List<Object> messages = new ArrayList<Object>();
				for (int i = 0; i < amount; i++)
					messages.add(i);
				callback.onMessage(messages, false);
				callback.onComplete();
			}

			@Override
			protected void continuingTrigger(Object request, int delay) {
				logger.info("continuing trigger");
				triggers.delayTrigger(request, delay);
			}
		};
		scheduler.setExecutor(new ThreadPoolExecutor(
				4, 8,
				300, TimeUnit.SECONDS,
				new SynchronousQueue<Runnable>(),
				Executors.defaultThreadFactory(),
				new ThreadPoolExecutor.CallerRunsPolicy()));
		scheduler.setPullStep(1);
		scheduler.setPullAmount(10);
		scheduler.setContinuingTriggerDelayMillis(1000);

		// connect
		manager.connectClient(clientId, new ClientConnection(clientId, null) {
			@Override
			public SendStatus sendMessage(Object msg) throws Exception {
				logger.info(msg.toString());
				return SendStatus.SENT;
			}

			@Override
			public boolean isOpen() {
				return true;
			}

			@Override
			public void close(String reasonText) {
			}
		});

		// trigger
		triggers.delayTrigger(clientId, 10);

		System.in.read();
	}
}