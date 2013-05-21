package com.taobao.top.mix;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.taobao.top.link.LinkException;
import com.taobao.top.link.endpoint.Endpoint;
import com.taobao.top.link.endpoint.EndpointContext;
import com.taobao.top.link.endpoint.EndpointProxy;
import com.taobao.top.link.endpoint.MessageHandler;

public class MixServerTest {
	private static URI uri;

	@BeforeClass
	public static void start() throws URISyntaxException {
		uri = new URI("ws://localhost:8080/");
		MixServer.start(uri.getPort());
	}

	@AfterClass
	public static void stop() throws InterruptedException {
		MixServer.stop();
	}

	@Test
	public void poll_test() throws LinkException {
		Endpoint e = new Endpoint(new ClientIdentity("app_poll"));
		e.setMessageHandler(new MessageHandler() {
			@Override
			public void onMessage(HashMap<String, String> message) {
				System.out.println("got reply:" + message);
			}

			@Override
			public void onMessage(EndpointContext context) throws Exception {
			}
		});
		EndpointProxy proxy = e.getEndpoint(new ServerIdentity(), uri);
		HashMap<String, String> msg = new HashMap<String, String>();
		msg.put("action", "polling");
		// proxy.send(msg);
		proxy.sendAndWait(msg);
	}

	@Test
	public void push_test() throws LinkException, InterruptedException {
		final CountDownLatch latch = new CountDownLatch(1);
		ClientIdentity id = new ClientIdentity("app_push");
		Endpoint e = new Endpoint(id);
		e.getEndpoint(new ServerIdentity(), uri);
		e.setMessageHandler(new MessageHandler() {
			@Override
			public void onMessage(HashMap<String, String> message) {
			}

			@Override
			public void onMessage(EndpointContext context) throws Exception {
				System.out.println("got push message:" + context.getMessage());
				HashMap<String, String> msg = new HashMap<String, String>();
				msg.put("confirm", "123");
				context.reply(msg);
				latch.countDown();
			}
		});
		// push to id
		MixServer.pending(id, new HashMap<String, String>());
		latch.await();
	}
}
