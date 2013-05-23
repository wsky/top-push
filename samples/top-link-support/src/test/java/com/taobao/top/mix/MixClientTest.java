package com.taobao.top.mix;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Test;

import com.taobao.top.link.LinkException;

public class MixClientTest {
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