package com.taobao.top.push.sample;

import static junit.framework.Assert.*;

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
import com.taobao.top.push.SendStatus;
import com.taobao.top.push.pulling.PullRequestScheduler;
import com.taobao.top.push.pulling.ClientPullings;

public class PullPushSample {
	static Logger logger = LoggerFactory.getLogger(PullPushSample.class);
	static PushManager manager;
	static ClientPullings pullings;
	static PullRequestScheduler scheduler;
	static Object clientId = "client-test";
	
	public static void main(String[] args) throws IOException, InterruptedException {
		manager = new PushManager();
		
		scheduler = new PullRequestScheduler() {
			@Override
			protected void pull(Object request, Client client, int amount, int pullStep, Callback callback) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				List<Object> messages = new ArrayList<Object>();
				for (int i = 0; i < amount; i++)
					messages.add(i);
				assertTrue(callback.onMessage(messages, false));
				callback.onComplete();
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
		manager.getOrCreateClient(clientId).addConnection(new ClientConnection(clientId, null) {
			@Override
			public SendStatus sendMessage(Object msg) throws Exception {
				logger.info(msg.toString());
				return SendStatus.SENT;
			}
			
			@Override
			public boolean isValid() {
				return true;
			}
		});
		
		pullings = new ClientPullings() {
			@Override
			protected List<Object> getPullRequests(Client client) {
				List<Object> requests = new ArrayList<Object>();
				requests.add("1");
				return requests;
			}
		};
		pullings.setPeriod(500);
		pullings.setScheduler(scheduler);
		
		pullings.add(manager.getClient(clientId));
		
		System.in.read();
	}
}
