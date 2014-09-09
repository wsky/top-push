package top.push.sample;

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

import top.push.Client;
import top.push.ClientConnection;
import top.push.PushManager;
import top.push.SendStatus;
import top.push.pulling.ClientPullings;
import top.push.pulling.PullRequestScheduler;

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
		scheduler.setContinuingDelayMillis(1000);
		
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
			protected Object getPullRequest(Client client) {
				return "request";
			}
		};
		pullings.setPeriod(500);
		pullings.setScheduler(scheduler);
		
		for (int i = 0; i < 5; i++)
			new Thread(new Runnable() {
				@Override
				public void run() {
					while (true)
						pullings.add(manager.getClient(clientId));
				}
			}).start();
		
		System.in.read();
	}
}
