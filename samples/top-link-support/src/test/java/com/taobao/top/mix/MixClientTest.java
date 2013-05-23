package com.taobao.top.mix;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Test;

import com.taobao.top.link.LinkException;
import com.taobao.top.mix.MixClient.MixClientHandler;
import com.taobao.top.mix.MixClient.MixMessage;

public class MixClientTest {
	@Test
	public void init_test() throws URISyntaxException, LinkException, InterruptedException {
		URI uri = new URI("ws://localhost:8080/");
		MixServer.start(uri.getPort());

		MixClient client = new MixClient(new ClientIdentity("mix_client"));
		client.setClientHandler(new MixClientHandler() {
			@Override
			public void onPushMessage(MixMessage message) throws Exception {
			}

			@Override
			public void onPollResult(MixMessage[] messages) {
			}
		});
		client.connect(uri);

		MixServer.stop();
	}

	@Test
	public void reconnect_test() throws URISyntaxException, LinkException, InterruptedException {
		URI uri = new URI("ws://localhost:8080/");
		MixServer.start(uri.getPort());
		MixClient client = new MixClient(new ClientIdentity("mix_client"));
		client.reconnectInterval = 200;
		client.connect(uri);

		MixServer.stop();
		Thread.sleep(500);
		MixServer.start(uri.getPort());

		Thread.sleep(500);
		client.poll();
	}
}