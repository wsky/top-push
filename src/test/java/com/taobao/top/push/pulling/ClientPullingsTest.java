package com.taobao.top.push.pulling;

import static org.junit.Assert.*;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import com.taobao.top.push.Client;

public class ClientPullingsTest {
	public static void main(String[] args) throws InterruptedException {
		ClientPullings pullings = new ClientPullings() {
			@Override
			protected Object getPullRequest(Client client) {
				return null;
			}
			
			@Override
			protected void dispatch(Client client) {
				System.out.println(client.getId());
			}
		};
		
		pullings.setPeriod(1000);
		
		Client[] clients = new Client[10];
		for (int i = 0; i < clients.length; i++)
			clients[i] = new Client(i);
		
		while (true) {
			for (int i = 0; i < clients.length; i++)
				pullings.add(clients[i]);
			Thread.sleep(10);
		}
	}
	
	@Test
	public void dispatch_test() throws InterruptedException {
		ClientPullings pullings = new ClientPullings() {
			@Override
			protected Object getPullRequest(Client client) {
				return "request";
			}
			
			@Override
			protected void dispatch(Client client, Object request) {
			}
		};
		pullings.add(new Client(null));
		pullings.dispatchAll();
	}
	
	@Test
	public void duplicate_test() {
		final AtomicInteger count = new AtomicInteger();
		ClientPullings pullings = new ClientPullings() {
			@Override
			protected Object getPullRequest(Client client) {
				return null;
			}
			
			@Override
			protected void dispatch(Client client) {
				count.incrementAndGet();
			}
		};
		
		Client client = new Client(null);
		for (int i = 0; i < 10000; i++)
			pullings.add(client);
		pullings.dispatchAll();
		assertEquals(1, count.get());
	}
}