package com.taobao.top.mix;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.taobao.top.link.LinkException;
import com.taobao.top.link.channel.ChannelException;

public class PerfTest {
	private static URI uri;
	private static MixClient client;

	@BeforeClass
	public static void beforeClass() throws URISyntaxException, LinkException {
		uri = new URI("ws://localhost:8080/");
		MixServer.start(uri.getPort());

		client = new MixClient(new ClientIdentity("perf"));
		client.connect(uri);
	}

	@AfterClass
	public static void afterClass() throws InterruptedException {
		MixServer.stop();
	}

	@Test
	public void connection_test() throws LinkException {
		for (int i = 0; i < 100; i++)
			new MixClient(new ClientIdentity("perf" + i)).connect(uri);
	}

	@Test
	public void poll_test() throws ChannelException {
		client.poll();
	}
	
	@Test
	public void poll_reject_test() throws ChannelException {
		client.poll();
	}
	
	@Test
	public void push_test(){
		
	}
	
	@Test
	public void push_hang_test(){
		
	}
}
