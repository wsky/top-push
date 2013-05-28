package com.taobao.top.mix;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.Test;

import com.taobao.top.link.LinkException;
import com.taobao.top.link.channel.ChannelException;

public class PerfTest {
	private static URI uri;
	private static MixClient client;

	static{
		try {
			beforeClass();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (LinkException e) {
			e.printStackTrace();
		}
	}

	public static void beforeClass() throws URISyntaxException, LinkException {
		uri = new URI("ws://localhost:8080/");
		//MixServer.start(uri.getPort());

		client = new MixClient(new ClientIdentity("perf"));
		client.connect(uri);
	}

	@AfterClass
	public static void afterClass() throws InterruptedException {
		//MixServer.stop();
	}

	@Test
	public void connection_test() throws LinkException {
		for (int i = 0; i < 10; i++)
			new MixClient(new ClientIdentity("perf" + i)).connect(uri);
	}

	@Test
	public void send_test() throws LinkException {
		Map<String, String> msg = new HashMap<String, String>();
		msg.put("str", "12345678901234567890123456789012345678901234567890123456789012345678901234567890");
		// client.send(msg);
		client.sendAndWait(msg, 2000);
	}

	@Test
	public void poll_reject_test() throws ChannelException {

	}

	@Test
	public void push_test() {

	}

	@Test
	public void push_hang_test() {

	}
}
